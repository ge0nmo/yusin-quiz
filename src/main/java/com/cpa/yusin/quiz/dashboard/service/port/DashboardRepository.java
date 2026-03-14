package com.cpa.yusin.quiz.dashboard.service.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DashboardRepository {

    long countActiveSubjects();

    long countActiveExams();

    long countActiveProblems();

    long countActiveQuestions();

    long countTodayQuestions(LocalDateTime startOfDay, LocalDateTime endOfDay);

    long countUnansweredQuestions();

    long countProblemsWithoutLecture();

    List<DashboardPendingQuestionProjection> findPendingQuestions(int limit);

    Optional<DashboardSubjectContextProjection> findSubjectContext(long subjectId);

    Optional<DashboardExamContextProjection> findExamContext(long examId, Long subjectId);
}
