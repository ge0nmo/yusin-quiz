package com.cpa.yusin.quiz.exam.service.port;

public interface ExamValidator
{
    void validate(long subjectId, String name, int year);
    void validate(long examId, long subjectId, String name, int year);
}
