package com.cpa.yusin.quiz.subject.service.port;

public interface SubjectValidator
{
    void validateName(String name);

    void validateName(long id, String name);
}
