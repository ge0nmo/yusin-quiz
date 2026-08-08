package com.cpa.yusin.quiz.problem.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCountProjection;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TeardownExtension.class)
@SpringBootTest
class WordProblemCatalogRepositoryTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Test
    void countsOnlyPublishedWordProblemsAndKeepsZeroCountSubjects() {
        Subject publishedSubject = subjectRepository.save(subject("공개 과목", SubjectStatus.PUBLISHED));
        Subject zeroProblemSubject = subjectRepository.save(subject("빈 공개 과목", SubjectStatus.PUBLISHED));
        Subject legacyPublishedSubject = subjectRepository.save(subject("레거시 공개 과목", null));
        Subject draftSubject = subjectRepository.save(subject("임시 과목", SubjectStatus.DRAFT));
        Subject removedSubject = subjectRepository.save(subject("삭제 과목", SubjectStatus.PUBLISHED));
        removedSubject.delete(100L);
        subjectRepository.save(removedSubject);

        Exam firstPublishedExam = examRepository.save(exam("2023 공개", 2023, publishedSubject.getId(), ExamStatus.PUBLISHED));
        Exam secondPublishedExam = examRepository.save(exam("2024 공개", 2024, publishedSubject.getId(), ExamStatus.PUBLISHED));
        Exam legacyPublishedExam = examRepository.save(exam("2024 레거시 공개", 2024, legacyPublishedSubject.getId(), ExamStatus.PUBLISHED));
        Exam draftExam = examRepository.save(exam("임시 시험", 2025, publishedSubject.getId(), ExamStatus.DRAFT));
        Exam removedExam = examRepository.save(exam("삭제 시험", 2025, publishedSubject.getId(), ExamStatus.PUBLISHED));
        removedExam.delete(100L);
        examRepository.save(removedExam);
        Exam draftSubjectExam = examRepository.save(exam("임시 과목 시험", 2024, draftSubject.getId(), ExamStatus.PUBLISHED));
        Exam removedSubjectExam = examRepository.save(exam("삭제 과목 시험", 2024, removedSubject.getId(), ExamStatus.PUBLISHED));

        Problem firstCandidate = problemRepository.save(problem(1, false, firstPublishedExam));
        Problem secondCandidate = problemRepository.save(problem(2, false, secondPublishedExam));
        Problem legacyCandidate = problemRepository.save(problem(1, false, legacyPublishedExam));
        problemRepository.save(problem(3, true, firstPublishedExam));
        Problem removedProblem = problemRepository.save(problem(4, false, firstPublishedExam));
        removedProblem.delete();
        problemRepository.save(removedProblem);
        problemRepository.save(problem(5, false, draftExam));
        problemRepository.save(problem(6, false, removedExam));
        problemRepository.save(problem(7, false, draftSubjectExam));
        problemRepository.save(problem(8, false, removedSubjectExam));

        Map<Long, Long> counts = problemRepository.countPublishedWordProblemsBySubject().stream()
                .collect(Collectors.toMap(WordProblemCountProjection::subjectId, WordProblemCountProjection::problemCount));

        assertThat(counts)
                .containsEntry(publishedSubject.getId(), 2L)
                .containsEntry(zeroProblemSubject.getId(), 0L)
                .containsEntry(legacyPublishedSubject.getId(), 1L)
                .doesNotContainKeys(draftSubject.getId(), removedSubject.getId());

        List<WordProblemCandidateProjection> candidates =
                problemRepository.findPublishedWordProblemCandidatesBySubjectId(publishedSubject.getId());

        assertThat(candidates).containsExactly(
                new WordProblemCandidateProjection(firstCandidate.getId(), firstPublishedExam.getId(), 2023),
                new WordProblemCandidateProjection(secondCandidate.getId(), secondPublishedExam.getId(), 2024)
        );
        assertThat(problemRepository.findPublishedWordProblemCandidatesBySubjectId(zeroProblemSubject.getId())).isEmpty();
        assertThat(problemRepository.findPublishedWordProblemCandidatesBySubjectId(legacyPublishedSubject.getId()))
                .containsExactly(new WordProblemCandidateProjection(
                        legacyCandidate.getId(), legacyPublishedExam.getId(), legacyPublishedExam.getYear()));
        assertThat(problemRepository.findPublishedWordProblemsByIds(List.of(legacyCandidate.getId())))
                .extracting(Problem::getId)
                .containsExactly(legacyCandidate.getId());
        assertThat(problemRepository.findPublishedWordProblemCandidatesBySubjectId(draftSubject.getId())).isEmpty();
        assertThat(problemRepository.findPublishedWordProblemCandidatesBySubjectId(removedSubject.getId())).isEmpty();
    }

    private Subject subject(String name, SubjectStatus status) {
        return Subject.builder().name(name).status(status).build();
    }

    private Exam exam(String name, int year, Long subjectId, ExamStatus status) {
        return Exam.builder().name(name).year(year).subjectId(subjectId).status(status).build();
    }

    private Problem problem(int number, boolean requiresCalculation, Exam exam) {
        return Problem.builder().number(number).requiresCalculation(requiresCalculation).exam(exam).build();
    }
}
