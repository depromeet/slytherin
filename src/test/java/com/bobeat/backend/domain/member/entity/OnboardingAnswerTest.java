package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class OnboardingAnswerTest {

    @Nested
    class 질문과옵션으로답변찾기 {

        @ParameterizedTest
        @CsvSource({
                "1, 1, Q1_CONVENIENT_FOOD",
                "1, 2, Q1_SINGLE_PORTION_FOOD", 
                "1, 3, Q1_REGULAR_FOOD",
                "1, 4, Q1_SHARING_FOOD",
                "2, 1, Q2_BAR_SEAT_ONLY",
                "2, 2, Q2_SINGLE_SEAT_PREFERRED",
                "2, 3, Q2_TABLE_SEAT_POSSIBLE", 
                "2, 4, Q2_ANY_SEAT_COMFORTABLE",
                "3, 1, Q3_VERY_UNCOMFORTABLE",
                "3, 2, Q3_NEED_PRIVATE_SEAT",
                "3, 3, Q3_SLIGHTLY_CONCERNED",
                "3, 4, Q3_NOT_CONCERNED_AT_ALL",
                "4, 1, Q4_CANNOT_AT_ALL",
                "4, 2, Q4_SOMETIMES_BUT_UNCOMFORTABLE",
                "4, 3, Q4_DEPENDS_ON_SITUATION",
                "4, 4, Q4_NO_PROBLEM_AT_ALL",
                "5, 1, Q5_QUICK_AND_SIMPLE",
                "5, 2, Q5_NEED_SINGLE_MENU",
                "5, 3, Q5_VARIETY_OF_MENU",
                "5, 4, Q5_NO_CONDITIONS_MATTER"
        })
        void 유효한질문번호와옵션으로올바른답변을찾을수있다(int questionNumber, int selectedOption, String expectedAnswer) {
            // when
            OnboardingAnswer result = OnboardingAnswer.findByQuestionAndOption(questionNumber, selectedOption);

            // then
            assertThat(result.name()).isEqualTo(expectedAnswer);
            assertThat(result.getQuestionNumber()).isEqualTo(questionNumber);
            assertThat(result.getScore()).isEqualTo(selectedOption);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 1",   // 잘못된 질문 번호
                "6, 1",   // 존재하지 않는 질문 번호
                "1, 0",   // 잘못된 옵션
                "1, 5",   // 존재하지 않는 옵션
                "-1, 1",  // 음수 질문 번호
                "1, -1"   // 음수 옵션
        })
        void 유효하지않은질문번호나옵션으로예외가발생한다(int questionNumber, int selectedOption) {
            // when & then
            assertThatThrownBy(() -> OnboardingAnswer.findByQuestionAndOption(questionNumber, selectedOption))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ONBOARDING_QUESTION);
        }
    }

    @Nested
    class 가중치적용점수계산 {

        @Test
        void Q1메뉴답변은1점2배가중치가적용된다() {
            // given
            OnboardingAnswer convenientFood = OnboardingAnswer.Q1_CONVENIENT_FOOD; // score: 1, weight: 1.2
            OnboardingAnswer sharingFood = OnboardingAnswer.Q1_SHARING_FOOD;       // score: 4, weight: 1.2

            // when & then
            assertThat(convenientFood.getWeightedScore()).isEqualTo(1.2);
            assertThat(sharingFood.getWeightedScore()).isEqualTo(4.8);
        }

        @Test
        void Q2좌석답변은1점2배가중치가적용된다() {
            // given
            OnboardingAnswer barSeatOnly = OnboardingAnswer.Q2_BAR_SEAT_ONLY;         // score: 1, weight: 1.2
            OnboardingAnswer anyComfortable = OnboardingAnswer.Q2_ANY_SEAT_COMFORTABLE; // score: 4, weight: 1.2

            // when & then
            assertThat(barSeatOnly.getWeightedScore()).isEqualTo(1.2);
            assertThat(anyComfortable.getWeightedScore()).isEqualTo(4.8);
        }

        @Test
        void Q3시선답변은1점0배가중치가적용된다() {
            // given
            OnboardingAnswer veryUncomfortable = OnboardingAnswer.Q3_VERY_UNCOMFORTABLE; // score: 1, weight: 1.0
            OnboardingAnswer notConcerned = OnboardingAnswer.Q3_NOT_CONCERNED_AT_ALL;   // score: 4, weight: 1.0

            // when & then
            assertThat(veryUncomfortable.getWeightedScore()).isEqualTo(1.0);
            assertThat(notConcerned.getWeightedScore()).isEqualTo(4.0);
        }

        @Test
        void Q4이인분도전답변은1점3배가중치가적용된다() {
            // given  
            OnboardingAnswer cannotAtAll = OnboardingAnswer.Q4_CANNOT_AT_ALL;     // score: 1, weight: 1.3
            OnboardingAnswer noProblem = OnboardingAnswer.Q4_NO_PROBLEM_AT_ALL;   // score: 4, weight: 1.3

            // when & then
            assertThat(cannotAtAll.getWeightedScore()).isEqualTo(1.3);
            assertThat(noProblem.getWeightedScore()).isEqualTo(5.2);
        }

        @Test
        void Q5중요도답변은1점0배가중치가적용된다() {
            // given
            OnboardingAnswer quickSimple = OnboardingAnswer.Q5_QUICK_AND_SIMPLE;      // score: 1, weight: 1.0
            OnboardingAnswer noConditions = OnboardingAnswer.Q5_NO_CONDITIONS_MATTER; // score: 4, weight: 1.0

            // when & then  
            assertThat(quickSimple.getWeightedScore()).isEqualTo(1.0);
            assertThat(noConditions.getWeightedScore()).isEqualTo(4.0);
        }
    }

    @Nested
    class 레벨계산 {

        @ParameterizedTest
        @CsvSource({
                "5.0, LEVEL_1",   // 입문자 최소값
                "7.0, LEVEL_1",   // 입문자 중간값
                "9.0, LEVEL_1",   // 입문자 최대값
                "10.0, LEVEL_2",  // 탐험가 최소값
                "12.0, LEVEL_2",  // 탐험가 중간값
                "14.0, LEVEL_2",  // 탐험가 최대값
                "15.0, LEVEL_3",  // 숙련자 최소값
                "17.0, LEVEL_3",  // 숙련자 중간값
                "19.0, LEVEL_3",  // 숙련자 최대값
                "20.0, LEVEL_4",  // 고수 최소값
                "23.0, LEVEL_4",  // 고수 중간값
                "26.0, LEVEL_4"   // 고수 최대값
        })
        void 점수구간에따라올바른레벨을반환한다(double totalScore, Level expectedLevel) {
            // when
            Level result = OnboardingAnswer.calculateLevel(totalScore);

            // then
            assertThat(result).isEqualTo(expectedLevel);
        }

        @Test
        void 점수가5미만이면LEVEL_1을반환한다() {
            // when
            Level result = OnboardingAnswer.calculateLevel(4.0);

            // then
            assertThat(result).isEqualTo(Level.LEVEL_1);
        }

        @Test
        void 점수가26초과이면LEVEL_4를반환한다() {
            // when
            Level result = OnboardingAnswer.calculateLevel(30.0);

            // then
            assertThat(result).isEqualTo(Level.LEVEL_4);
        }

        @Test
        void 점수반올림이올바르게작동한다() {
            // when & then
            assertThat(OnboardingAnswer.calculateLevel(9.4)).isEqualTo(Level.LEVEL_1);  // 반올림하면 9
            assertThat(OnboardingAnswer.calculateLevel(9.5)).isEqualTo(Level.LEVEL_2);  // 반올림하면 10
            assertThat(OnboardingAnswer.calculateLevel(14.4)).isEqualTo(Level.LEVEL_2); // 반올림하면 14
            assertThat(OnboardingAnswer.calculateLevel(14.5)).isEqualTo(Level.LEVEL_3); // 반올림하면 15
        }
    }
}