package com.cpa.yusin.quiz.member.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomNicknameGenerator {

    private static final int RANDOM_BOUND = 10_000;
    private static final int DIGIT_LENGTH = 4;

    // 1. Arrays.asList로 배열 생성
    // 2. LinkedHashSet으로 변환하여 '중복 데이터' 자동 제거 (에러 없이 조용히 제거됨)
    // 3. List.copyOf로 감싸서 읽기 전용(Immutable) List로 최종 고정 (O(1) 조회 성능 확보)
    private static final List<String> ADJECTIVES = List.copyOf(
            new LinkedHashSet<>(Arrays.asList(
                    "행복한", "즐거운", "용감한", "똑똑한", "성실한", "친절한", "현명한", "귀여운", "멋진", "강한",
                    "따뜻한", "부지런한", "명랑한", "조용한", "차분한", "활기찬", "긍정적인", "자유로운", "신나는", "당당한",
                    "지혜로운", "기운찬", "상쾌한", "푸근한", "눈부신", "열정적인", "씩씩한", "다정한", "재빠른", "빛나는",
                    "엉뚱한", "호기심많은", "재미있는", "유쾌한", "사랑스러운", "슬기로운", "정직한", "침착한", "끈기있는", "듬직한",
                    "사려깊은", "상냥한", "섬세한", "수줍은", "순수한", "신중한", "예의바른", "용기있는", "인내심강한", "자신감넘치는",
                    "적극적인", "점잖은", "정다운", "진지한", "참된", "쾌활한", "태평한", "튼튼한", "평화로운", "포근한",
                    "한결같은", "화목한", "화사한", "활발한", "훌륭한", "희망찬", "힘찬", "거대한", "날렵한", "다재다능한",
                    "단단한", "담대한", "대담한", "도전적인", "독창적인", "똑부러진", "매력적인", "부드러운", "분주한", "비범한",
                    "산뜻한", "새로운", "생기있는", "선량한", "세련된", "소중한", "솔직한", "시원한", "신비한", "아름다운",
                    "안전한", "알찬", "야무진", "여유로운", "영리한", "우아한", "유연한", "은은한", "자애로운", "자유분방한",
                    "달콤한", "매콤한", "짭짤한", "향기로운", "차가운", "뜨거운", "바삭한", "쫄깃한", "폭신한", "말랑한",
                    "쫀득한", "뽀짝한", "꼬물거리는", "뒤뚱거리는", "똘망똘망한", "엉뚱발랄한", "느긋한", "배고픈", "배부른", "졸린",
                    "말똥말똥한", "심심한", "신비로운", "기발한", "거침없는", "변덕스러운", "단호한", "찬란한", "고요한", "촉촉한",
                    "몽환적인", "우렁찬", "아득한", "투명한", "새하얀", "새까만", "푸르른", "노란", "빨간", "느릿느릿한",
                    "재잘거리는", "당돌한", "오동통한", "살랑거리는", "바쁘다바빠", "새침한", "수다스러운", "오싹한"
            ))
    );

    // 내부적으로 중복(부엉이, 햄스터 등)이 알아서 제거된 후 List로 셋팅됩니다.
    private static final List<String> NOUNS = List.copyOf(
            new LinkedHashSet<>(Arrays.asList(
                    "사자", "호랑이", "독수리", "고래", "펭귄", "강아지", "고양이", "토끼", "다람쥐", "판다",
                    "코알라", "북극곰", "돌고래", "오리", "거북이", "부엉이", "햄스터", "기린", "코끼리", "수달",
                    "여우", "늑대", "사슴", "곰", "물개", "갈매기", "매", "홍학", "앵무새", "두더지",
                    "낙타", "너구리", "당나귀", "도마뱀", "딱따구리", "라마", "물소", "미어캣", "바다표범",
                    "반달가슴곰", "백조", "부엉이", "불곰", "비버", "산양", "살쾡이", "상어", "수리부엉이", "스컹크",
                    "시베리안허스키", "심해어", "아기사슴", "악어", "알파카", "얼룩말", "염소", "오소리", "원숭이", "재규어",
                    "저빌", "제비", "지타", "참새", "청설모", "치타", "카멜레온", "캥거루", "코뿔소", "코요테",
                    "크로커다일", "타조", "표범", "퓨마", "하마", "해달", "해마", "햄스터", "향유고래", "흑곰",
                    "흰수염고래", "개미핥기", "고릴라", "고슴도치", "기러기", "나무늘보", "두루미", "물총새", "바다거북", "방울새",
                    "쿼카", "카피바라", "해파리", "문어", "오징어", "불가사리", "가오리", "슬라임", "마카롱", "붕어빵",
                    "떡볶이", "솜사탕", "푸딩", "젤리", "호떡", "츄러스", "와플", "초콜릿", "쿠키", "사과",
                    "바나나", "딸기", "포도", "복숭아", "수박", "귤", "오렌지", "레몬", "체리", "구름",
                    "바람", "별", "달", "해", "우주", "은하수", "무지개", "바다", "마법사", "요정",
                    "용사", "탐험가", "로봇", "도토리", "솔방울", "눈사람", "선인장", "은하", "혜성", "블랙홀",
                    "두쫀쿠", "탕후루", "범고래", "바다거북"
            ))
    );

    /**
     * 무작위 닉네임을 생성합니다. (형용사 + 명사 + 4자리 숫자)
     * @return 생성된 닉네임 (예: 행복한사자0042)
     */
    public String generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(NOUNS.size()));
        int randomNumber = random.nextInt(RANDOM_BOUND);

        // String.format 병목 제거 및 메모리 최적화
        int exactCapacity = adjective.length() + noun.length() + DIGIT_LENGTH;
        StringBuilder sb = new StringBuilder(exactCapacity);

        sb.append(adjective).append(noun);

        // 빠른 Zero-padding
        if (randomNumber < 10) {
            sb.append("000");
        } else if (randomNumber < 100) {
            sb.append("00");
        } else if (randomNumber < 1000) {
            sb.append("0");
        }
        sb.append(randomNumber);

        return sb.toString();
    }
}