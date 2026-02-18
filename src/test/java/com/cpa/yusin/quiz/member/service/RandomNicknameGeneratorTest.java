package com.cpa.yusin.quiz.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomNicknameGeneratorTest {

    private final RandomNicknameGenerator generator = new RandomNicknameGenerator();

    @Test
    @DisplayName("랜덤 닉네임 생성 확인")
    void generate() {
        // when
        String nickname = generator.generate();

        // then
        assertThat(nickname).isNotNull();
        assertThat(nickname).isNotEmpty();
        // 한글만 포함 여부 (숫자 없음)
        assertThat(nickname).matches("^[가-힣]+$");
        assertThat(nickname).doesNotContainAnyWhitespaces();
        assertThat(nickname).doesNotContainPattern("[0-9]");
    }
}
