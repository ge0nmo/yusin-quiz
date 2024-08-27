package com.cpa.yusin.quiz.common.service;

public interface CascadeDeleteService
{
    void deleteSubjectBySubjectId(long subjectId);

    void deleteExamByExamId(long examId);

}
