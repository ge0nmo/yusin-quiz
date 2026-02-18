package com.cpa.yusin.quiz.study.event;

import com.cpa.yusin.quiz.study.service.StudyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyLogService studyLogService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStudySolved(StudySolvedEvent event) {
        studyLogService.recordActivity(event.memberId(), event.solvedCount());
    }
}
