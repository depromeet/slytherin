package com.bobeat.backend.domain.member.entity;

import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum OnboardingAnswer {
    // Q1. 혼밥할 때 주로 어떤 메뉴를 드세요?
    Q1_CONVENIENT_FOOD(1, 1, 1.2, "간편식/즉석식, 패스트푸드, 분식류"),
    Q1_SINGLE_PORTION_FOOD(1, 2, 1.2, "국밥, 라멘, 돈까스, 카레, 초밥, 카페/디저트"),
    Q1_REGULAR_FOOD(1, 3, 1.2, "백반, 파스타, 중국집, 냉면, 칼국수"),
    Q1_SHARING_FOOD(1, 4, 1.2, "닭갈비, 찜닭, 고기구이, 감자탕/전골, 족발/보쌈, 샤브샤브"),

    // Q2. 혼밥할 때 어떤 좌석이 편하세요?
    Q2_BAR_SEAT_ONLY(2, 1, 1.2, "바(bar) 좌석만 찾아요"),
    Q2_SINGLE_SEAT_PREFERRED(2, 2, 1.2, "1~2인석을 선호해요"),
    Q2_TABLE_SEAT_POSSIBLE(2, 3, 1.2, "4인석, 눈치 보이지만 앉을 수 있어요"),
    Q2_ANY_SEAT_COMFORTABLE(2, 4, 1.2, "어떤 좌석이든 상관없어요"),

    // Q3. 혼밥할 때 주변 시선이 부담스러운 적이 있나요?
    Q3_VERY_UNCOMFORTABLE(3, 1, 1.0, "너무 부담돼서 혼밥을 잘 못 해요"),
    Q3_NEED_PRIVATE_SEAT(3, 2, 1.0, "혼밥석이 있으면 괜찮아요"),
    Q3_SLIGHTLY_CONCERNED(3, 3, 1.0, "조금 신경 쓰이는 정도예요"),
    Q3_NOT_CONCERNED_AT_ALL(3, 4, 1.0, "전혀 신경쓰지 않아요"),

    // Q4. 원한다면 2인 메뉴도 혼자 도전할 수 있나요?
    Q4_CANNOT_AT_ALL(4, 1, 1.3, "전혀 못 해요"),
    Q4_SOMETIMES_BUT_UNCOMFORTABLE(4, 2, 1.3, "가끔 먹지만 부담돼요"),
    Q4_DEPENDS_ON_SITUATION(4, 3, 1.3, "상황에 따라 시도할 수 있어요"),
    Q4_NO_PROBLEM_AT_ALL(4, 4, 1.3, "네, 혼자서도 문제없어요"),

    // Q5. 혼밥할 때 어떤 점이 가장 중요하신가요?
    Q5_QUICK_AND_SIMPLE(5, 1, 1.0, "빠르고 간편하게 먹을 수 있어야 해요"),
    Q5_NEED_SINGLE_MENU(5, 2, 1.0, "1인 메뉴가 있어야 해요"),
    Q5_VARIETY_OF_MENU(5, 3, 1.0, "다양한 메뉴를 즐길 수 있어야 해요"),
    Q5_NO_CONDITIONS_MATTER(5, 4, 1.0, "원하는 음식이면 조건은 상관없어요");

    private final int questionNumber;
    private final int score;
    private final double weight;
    private final String description;

    /**
     * 질문 번호와 선택한 옵션으로 OnboardingAnswer를 찾아 반환
     */
    public static OnboardingAnswer findByQuestionAndOption(int questionNumber, int selectedOption) {
        return Arrays.stream(OnboardingAnswer.values())
                .filter(answer -> answer.questionNumber == questionNumber && answer.score == selectedOption)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ONBOARDING_QUESTION));
    }

    /**
     * 가중치가 적용된 점수 계산
     */
    public double getWeightedScore() {
        return this.score * this.weight;
    }

    /**
     * 총 점수로 레벨 계산
     */
    public static Level calculateLevel(double totalWeightedScore) {
        int roundedScore = (int) Math.round(totalWeightedScore);
        
        if (roundedScore >= 5 && roundedScore <= 9) return Level.LEVEL_1;    // 입문자: 5~9점
        if (roundedScore >= 10 && roundedScore <= 14) return Level.LEVEL_2;  // 탐험가: 10~14점  
        if (roundedScore >= 15 && roundedScore <= 19) return Level.LEVEL_3;  // 숙련자: 15~19점
        if (roundedScore >= 20 && roundedScore <= 26) return Level.LEVEL_4;  // 고수: 20~26점
        
        // 예외 처리
        if (roundedScore < 5) return Level.LEVEL_1;
        return Level.LEVEL_4;
    }
}
