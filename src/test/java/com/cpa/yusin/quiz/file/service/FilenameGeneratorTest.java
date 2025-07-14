package com.cpa.yusin.quiz.file.service;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.mock.FakeUuidHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilenameGeneratorTest
{
    FilenameGenerator filenameGenerator;
    UuidHolder uuidHolder;

    @BeforeEach
    void setUp()
    {
        uuidHolder = new FakeUuidHolder("random-uuid-test");
        filenameGenerator = new FilenameGenerator(uuidHolder);
    }

    @Test
    void createStoreFileName()
    {
        // given
        String originalFileName = "test.png";

        // when
        String result = filenameGenerator.createStoreFileName(originalFileName);

        // then
        assertThat(result).isEqualTo("random-uuid-test.png");
    }
}