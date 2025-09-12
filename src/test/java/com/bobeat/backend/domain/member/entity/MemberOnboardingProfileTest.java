package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.domain.member.dto.request.OnboardingAnswerDto;
import com.bobeat.backend.domain.member.dto.request.OnboardingRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class MemberOnboardingProfileTest {

    @Nested
    class 온보딩프로필생성 {

        @Test
        void 회원과온보딩요청으로프로필을생성할수있다() {
            // given
            Member member = createTestMember();
            OnboardingRequest request = createValidOnboardingRequest();

            // when
            MemberOnboardingProfile profile = MemberOnboardingProfile.create(member, request);

            // then
            assertThat(profile.getMember()).isEqualTo(member);
            assertThat(profile.getQuestion1()).isEqualTo(OnboardingAnswer.Q1_CONVENIENT_FOOD);
            assertThat(profile.getQuestion2()).isEqualTo(OnboardingAnswer.Q2_BAR_SEAT_ONLY);
            assertThat(profile.getQuestion3()).isEqualTo(OnboardingAnswer.Q3_VERY_UNCOMFORTABLE);
            assertThat(profile.getQuestion4()).isEqualTo(OnboardingAnswer.Q4_CANNOT_AT_ALL);
            assertThat(profile.getQuestion5()).isEqualTo(OnboardingAnswer.Q5_QUICK_AND_SIMPLE);
        }
    }

    @Nested
    class 혼밥레벨계산및업데이트 {

        @ParameterizedTest
        @MethodSource("provideLevelCalculationTestCases")
        void 답변조합에따라올바른레벨이계산된다(
                List<OnboardingAnswerDto> answers, 
                Level expectedLevel, 
                String description) {
            // given
            Member member = createTestMember();
            OnboardingRequest request = new OnboardingRequest(answers);
            MemberOnboardingProfile profile = MemberOnboardingProfile.create(member, request);

            // when
            Level calculatedLevel = profile.calculateAndUpdateHonbapLevel();

            // then
            assertThat(calculatedLevel).isEqualTo(expectedLevel);
            assertThat(profile.getHonbapLevel()).isEqualTo(expectedLevel);
        }

        static Stream<Arguments> provideLevelCalculationTestCases() {
            return Stream.of(
                    Arguments.of(
                            List.of(
                                    new OnboardingAnswerDto(1, 1), // 1 * 1.2 = 1.2
                                    new OnboardingAnswerDto(2, 1), // 1 * 1.2 = 1.2  
                                    new OnboardingAnswerDto(3, 1), // 1 * 1.0 = 1.0
                                    new OnboardingAnswerDto(4, 1), // 1 * 1.3 = 1.3
                                    new OnboardingAnswerDto(5, 1)  // 1 * 1.0 = 1.0
                                    // 총합: 5.7점 -> LEVEL_1
                            ),
                            Level.LEVEL_1,
                            "혼밥 입문자 - 모든 답변 최저점"
                    ),
                    Arguments.of(
                            List.of(
                                    new OnboardingAnswerDto(1, 2), // 2 * 1.2 = 2.4
                                    new OnboardingAnswerDto(2, 2), // 2 * 1.2 = 2.4
                                    new OnboardingAnswerDto(3, 2), // 2 * 1.0 = 2.0
                                    new OnboardingAnswerDto(4, 2), // 2 * 1.3 = 2.6
                                    new OnboardingAnswerDto(5, 2)  // 2 * 1.0 = 2.0
                                    // 총합: 11.4점 -> LEVEL_2
                            ),
                            Level.LEVEL_2,
                            "혼밥 탐험가 - 중간 점수 조합"
                    ),
                    Arguments.of(
                            List.of(
                                    new OnboardingAnswerDto(1, 3), // 3 * 1.2 = 3.6
                                    new OnboardingAnswerDto(2, 3), // 3 * 1.2 = 3.6
                                    new OnboardingAnswerDto(3, 3), // 3 * 1.0 = 3.0
                                    new OnboardingAnswerDto(4, 3), // 3 * 1.3 = 3.9
                                    new OnboardingAnswerDto(5, 3)  // 3 * 1.0 = 3.0
                                    // 총합: 17.1점 -> LEVEL_3
                            ),
                            Level.LEVEL_3,
                            "혼밥 숙련자 - 높은 점수 조합"
                    ),
                    Arguments.of(
                            // 고수 케이스: 모든 답변이 4점 (최고점)
                            List.of(
                                    new OnboardingAnswerDto(1, 4), // 4 * 1.2 = 4.8
                                    new OnboardingAnswerDto(2, 4), // 4 * 1.2 = 4.8
                                    new OnboardingAnswerDto(3, 4), // 4 * 1.0 = 4.0
                                    new OnboardingAnswerDto(4, 4), // 4 * 1.3 = 5.2
                                    new OnboardingAnswerDto(5, 4)  // 4 * 1.0 = 4.0
                                    // 총합: 22.8점 -> LEVEL_4
                            ),
                            Level.LEVEL_4,
                            "혼밥 고수 - 모든 답변 최고점"
                    )
            );
        }

        @Test
        void 가중치가적용된점수계산이올바르게작동한다() {
            // given: Q4(2인분 도전)에 높은 점수, 나머지는 낮은 점수
            Member member = createTestMember();
            OnboardingRequest request = new OnboardingRequest(List.of(
                    new OnboardingAnswerDto(1, 1), // 1 * 1.2 = 1.2 (메뉴)
                    new OnboardingAnswerDto(2, 1), // 1 * 1.2 = 1.2 (좌석)  
                    new OnboardingAnswerDto(3, 1), // 1 * 1.0 = 1.0 (시선)
                    new OnboardingAnswerDto(4, 4), // 4 * 1.3 = 5.2 (2인분 도전 - 가장 높은 가중치)
                    new OnboardingAnswerDto(5, 1)  // 1 * 1.0 = 1.0 (중요도)
                    // 총합: 9.6점 -> 반올림하면 10점 -> LEVEL_2
            ));
            MemberOnboardingProfile profile = MemberOnboardingProfile.create(member, request);

            // when
            Level calculatedLevel = profile.calculateAndUpdateHonbapLevel();

            // then
            assertThat(calculatedLevel).isEqualTo(Level.LEVEL_2);
        }

        @Test
        void Q4이인분도전의높은가중치가레벨에결정적영향을미친다() {
            // given: Q4만 높고 나머지는 중간 정도
            Member member = createTestMember();
            OnboardingRequest request = new OnboardingRequest(List.of(
                    new OnboardingAnswerDto(1, 2), // 2 * 1.2 = 2.4
                    new OnboardingAnswerDto(2, 2), // 2 * 1.2 = 2.4
                    new OnboardingAnswerDto(3, 2), // 2 * 1.0 = 2.0
                    new OnboardingAnswerDto(4, 4), // 4 * 1.3 = 5.2 (결정적 요인)
                    new OnboardingAnswerDto(5, 2)  // 2 * 1.0 = 2.0
                    // 총합: 14.0점 -> LEVEL_2
            ));
            MemberOnboardingProfile profile = MemberOnboardingProfile.create(member, request);

            // when
            Level calculatedLevel = profile.calculateAndUpdateHonbapLevel();

            // then
            assertThat(calculatedLevel).isEqualTo(Level.LEVEL_2);
        }
    }

    private Member createTestMember() {
        // 테스트용 Member 객체 생성 (실제 구현에 맞게 조정)
        return new Member(); // 실제로는 적절한 Member 생성 로직 필요
    }

    private OnboardingRequest createValidOnboardingRequest() {
        return new OnboardingRequest(List.of(
                new OnboardingAnswerDto(1, 1), // Q1: 간편식
                new OnboardingAnswerDto(2, 1), // Q2: 바 좌석만  
                new OnboardingAnswerDto(3, 1), // Q3: 매우 부담
                new OnboardingAnswerDto(4, 1), // Q4: 전혀 못함
                new OnboardingAnswerDto(5, 1)  // Q5: 빠르고 간편
        ));
    }
}
