package com.cpa.yusin.quiz.problem.service.dto;

public record AdminProblemSearchCondition(AdminProblemLectureStatus lectureStatus,
                                          Long subjectId,
                                          Integer year,
                                          Long examId) {

    public static AdminProblemSearchCondition of(AdminProblemLectureStatus lectureStatus,
                                                 Long subjectId,
                                                 Integer year,
                                                 Long examId) {
        AdminProblemLectureStatus normalizedLectureStatus = lectureStatus == null
                ? AdminProblemLectureStatus.ALL
                : lectureStatus;

        return new AdminProblemSearchCondition(normalizedLectureStatus, subjectId, year, examId);
    }
}
