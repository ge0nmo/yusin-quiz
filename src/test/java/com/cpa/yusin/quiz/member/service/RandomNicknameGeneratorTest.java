package com.cpa.yusin.quiz.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

class RandomNicknameGeneratorTest {

    private final RandomNicknameGenerator generator = new RandomNicknameGenerator();

    // @Test 대신 @RepeatedTest를 사용하여 난수 생성의 엣지 케이스(0001 등)를 철저히 검증합니다.
    @RepeatedTest(value = 100, name = "{displayName} - {currentRepetition}/{totalRepetitions}")
    @DisplayName("랜덤 닉네임 생성 확인 (한글 + 4자리 숫자)")
    void generate() {
        // when
        String nickname = generator.generate();

        // then
        assertThat(nickname).isNotNull();
        assertThat(nickname).isNotEmpty();
        assertThat(nickname).doesNotContainAnyWhitespaces();

        // 한글(가~힣)이 1자 이상 연속된 후, 정확히 숫자(0~9) 4자리로 끝나는지 검증
        assertThat(nickname).matches("^[가-힣]+[0-9]{4}$");
    }
}