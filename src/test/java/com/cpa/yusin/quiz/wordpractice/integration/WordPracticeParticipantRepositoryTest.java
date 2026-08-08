package com.cpa.yusin.quiz.wordpractice.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeParticipantJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(TeardownExtension.class)
@SpringBootTest
class WordPracticeParticipantRepositoryTest {

    @Autowired
    private WordPracticeParticipantJpaRepository participantJpaRepository;

    @Test
    void typeAndOwnerKeyMustBeUnique() {
        participantJpaRepository.saveAndFlush(WordPracticeParticipant.member(42L));

        assertThatThrownBy(() -> participantJpaRepository.saveAndFlush(WordPracticeParticipant.member(42L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
