package com.cpa.yusin.quiz.study.controller.dto.response;

import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.StudySessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ExamStartResponse {
    private Long sessionId;
    private StudySessionStatus status;
    private int lastIndex;
    private List<SubmittedAnswerResponse> submittedAnswers;

    public static ExamStartResponse of(StudySession session, List<SubmittedAnswerResponse> answers) {
        return ExamStartResponse.builder()
                .sessionId(session.getId())
                .status(session.getStatus())
                .lastIndex(session.getLastIndex())
                .submittedAnswers(answers)
                .build();
    }
}
