package com.bobeat.backend.domain.security.oauth;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {
    private static final List<String> ADJECTIVES = List.of(
            "배고픈", "잠자는", "춤추는", "뛰어난", "멍때리는", "방황하는",
            "수다스러운", "귀차니즘", "흥분한", "뚝딱이는", "재채기하는", "뚜벅이는",
            "헛소리하는", "겁먹은", "버릇없는", "의심스러운", "꿈꾸는", "소심한",
            "엉뚱한", "알쏭달쏭한", "반항적인", "집중 안 하는", "장난꾸러기", "딴청부리는",
            "황당한", "잔망스러운", "수줍은", "쓸데없이 진지한", "호기심 많은", "허둥대는",
            "슬기로운", "자고 싶은", "헝클어진", "엄청난", "제멋대로인", "튕기는", "좌절한",
            "곱창을 사랑하는"
    );

    // 명사 리스트를 음식 이름으로 변경
    private static final List<String> FOODS = List.of(
            "피자", "치킨", "햄버거", "떡볶이", "김치찌개", "파스타", "삼겹살",
            "초밥", "라면", "카레", "돈까스", "우동", "짜장면", "스테이크",
            "부대찌개", "비빔밥", "샐러드", "샌드위치", "타코", "쌀국수",
            "마라탕", "양꼬치", "곱창", "막창", "족발", "보쌈", "감자탕",
            "와플", "마카롱", "아이스크림", "붕어빵", "호떡", "탕후루", "도넛"
    );

    private static final Random RANDOM = new Random();

    public static String getRandomNickname() {
        String nickName;
        do {
            String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
            String food = FOODS.get(RANDOM.nextInt(FOODS.size())); // 수정한 부분
            nickName = adjective + " " + food; // 수정한 부분
        }
        while (nickName.length() > 11);

        return nickName;
    }
}
