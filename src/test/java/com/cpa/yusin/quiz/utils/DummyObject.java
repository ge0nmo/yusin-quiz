package com.cpa.yusin.quiz.utils;

import com.cpa.yusin.quiz.domain.entity.Subject;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

public class DummyObject {
    public Subject mockSubject(Long id, String name) throws NoSuchFieldException {
        Subject subject = Subject.builder()
                .name(name)
                .build();

        ReflectionTestUtils.setField(subject, "id", id);

        return subject;
    }
}
