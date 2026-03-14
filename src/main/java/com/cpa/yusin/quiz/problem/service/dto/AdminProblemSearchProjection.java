package com.cpa.yusin.quiz.problem.service.dto;

import com.cpa.yusin.quiz.problem.domain.Problem;

public record AdminProblemSearchProjection(Problem problem,
                                           Long subjectId,
                                           String subjectName,
                                           Long examId,
                                           String examName,
                                           int examYear,
                                           long choiceCount,
                                           long answerChoiceCount) {
}
