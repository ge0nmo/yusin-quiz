package com.cpa.yusin.quiz.problem.controller.dto.response;

public record AdminProblemSearchResponse(Long id,
                                         int number,
                                         Long subjectId,
                                         String subjectName,
                                         Long examId,
                                         String examName,
                                         int examYear,
                                         ProblemLectureResponse lecture,
                                         long choiceCount,
                                         long answerChoiceCount,
                                         String contentPreviewText) {
}
