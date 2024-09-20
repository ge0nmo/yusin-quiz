package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;

public class MockSetup
{
    protected TestContainer testContainer;

    protected SubjectDomain physics;
    protected SubjectDomain biology;

    protected ExamDomain physicsExam1;
    protected ExamDomain physicsExam2;
    protected ExamDomain biologyExam1;
    protected ExamDomain biologyExam2;

    protected ProblemDomain physicsProblem1;
    protected ProblemDomain physicsProblem2;

    protected ChoiceDomain choice1;
    protected ChoiceDomain choice2;
    protected ChoiceDomain choice3;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        physics = testContainer.subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("Physics")
                .build());

        biology = testContainer.subjectRepository.save(biology = SubjectDomain.builder()
                .id(2L)
                .name("Biology")
                .build());

        physicsExam1 = testContainer.examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("2024 1차")
                .year(2024)
                .subjectId(physics.getId())
                .build());

        physicsExam2 = testContainer.examRepository.save(ExamDomain.builder()
                .id(2L)
                .name("2024 2차")
                .year(2024)
                .subjectId(physics.getId())
                .build());

        biologyExam1 = testContainer.examRepository.save(ExamDomain.builder()
                .id(3L)
                .name("2024 1차")
                .year(2024)
                .subjectId(biology.getId())
                .build());

        biologyExam2 = testContainer.examRepository.save(ExamDomain.builder()
                .id(4L)
                .name("2024 2차")
                .year(2024)
                .subjectId(biology.getId())
                .build());


        physicsProblem1 = testContainer.problemRepository.save(ProblemDomain.builder()
                .id(1L)
                .content("content abc")
                .number(1)
                .exam(physicsExam1)
                .build());

        physicsProblem2 = testContainer.problemRepository.save(ProblemDomain.builder()
                .id(2L)
                .content("content zxc")
                .number(2)
                .exam(physicsExam1)
                .build());


        choice1 = testContainer.choiceRepository.save(ChoiceDomain.builder()
                .id(1L)
                .content("choice 1")
                .number(1)
                .isAnswer(true)
                .problem(physicsProblem1)
                .build());

        choice2 = testContainer.choiceRepository.save(ChoiceDomain.builder()
                .id(2L)
                .content("choice 2")
                .number(2)
                .isAnswer(false)
                .problem(physicsProblem1)
                .build());

        choice3 = testContainer.choiceRepository.save(ChoiceDomain.builder()
                .id(3L)
                .content("choice 3")
                .number(3)
                .isAnswer(false)
                .problem(physicsProblem1)
                .build());
    }
}
