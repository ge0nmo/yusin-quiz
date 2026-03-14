package com.cpa.yusin.quiz.dashboard.infrastructure;

import com.cpa.yusin.quiz.dashboard.service.port.DashboardExamContextProjection;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardPendingQuestionProjection;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardRepository;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardSubjectContextProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class DashboardRepositoryImpl implements DashboardRepository {

    private final DashboardJpaRepository dashboardJpaRepository;

    @Override
    public long countActiveSubjects() {
        return dashboardJpaRepository.countActiveSubjects();
    }

    @Override
    public long countActiveExams() {
        return dashboardJpaRepository.countActiveExams();
    }

    @Override
    public long countActiveProblems() {
        return dashboardJpaRepository.countActiveProblems();
    }

    @Override
    public long countActiveQuestions() {
        return dashboardJpaRepository.countActiveQuestions();
    }

    @Override
    public long countTodayQuestions(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return dashboardJpaRepository.countTodayQuestions(startOfDay, endOfDay);
    }

    @Override
    public long countUnansweredQuestions() {
        return dashboardJpaRepository.countUnansweredQuestions();
    }

    @Override
    public long countProblemsWithoutLecture() {
        return dashboardJpaRepository.countProblemsWithoutLecture();
    }

    @Override
    public List<DashboardPendingQuestionProjection> findPendingQuestions(int limit) {
        return dashboardJpaRepository.findPendingQuestions(PageRequest.of(0, limit));
    }

    @Override
    public Optional<DashboardSubjectContextProjection> findSubjectContext(long subjectId) {
        return dashboardJpaRepository.findSubjectContext(subjectId);
    }

    @Override
    public Optional<DashboardExamContextProjection> findExamContext(long examId, Long subjectId) {
        return dashboardJpaRepository.findExamContext(examId, subjectId);
    }
}
