package com.cpa.yusin.quiz.utils;

import com.cpa.yusin.quiz.subject.infrastructure.Subject;
import org.springframework.test.util.ReflectionTestUtils;

public class DummyObject {
    public Subject mockSubject(Long id, String name) throws NoSuchFieldException {
        Subject subject = Subject.builder()
                .name(name)
                .build();

        ReflectionTestUtils.setField(subject, "id", id);

        return subject;
    }
}
