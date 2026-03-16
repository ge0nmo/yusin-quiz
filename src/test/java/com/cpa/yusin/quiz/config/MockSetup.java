package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.junit.jupiter.api.BeforeEach;

public class MockSetup {
        protected TestContainer testContainer;

        protected Member member1;

        protected Subject physics;
        protected Subject biology;

        protected Exam physicsExam1;
        protected Exam physicsExam2;
        protected Exam biologyExam1;
        protected Exam biologyExam2;

        protected Problem physicsProblem1;
        protected Problem physicsProblem2;

        protected Choice choice1;
        protected Choice choice2;
        protected Choice choice3;

        @BeforeEach
        void setUp() {
                testContainer = new TestContainer();

                member1 = testContainer.memberRepository.save(Member.builder()
                                .id(1L)
                                .email("test@test.com")
                                .password("password")
                                .username("testuser")
                                .platform(Platform.HOME)
                                .role(Role.USER)
                                .build());

                physics = testContainer.subjectRepository.save(Subject.builder()
                                .id(1L)
                                .name("Physics")
                                .build());

                biology = testContainer.subjectRepository.save(biology = Subject.builder()
                                .id(2L)
                                .name("Biology")
                                .build());

                physicsExam1 = testContainer.examRepository.save(Exam.builder()
                                .id(1L)
                                .name("2024 1차")
                                .year(2024)
                                .subjectId(physics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                physicsExam2 = testContainer.examRepository.save(Exam.builder()
                                .id(2L)
                                .name("2024 2차")
                                .year(2024)
                                .subjectId(physics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                biologyExam1 = testContainer.examRepository.save(Exam.builder()
                                .id(3L)
                                .name("2024 1차")
                                .year(2024)
                                .subjectId(biology.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                biologyExam2 = testContainer.examRepository.save(Exam.builder()
                                .id(4L)
                                .name("2024 2차")
                                .year(2024)
                                .subjectId(biology.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                physicsProblem1 = testContainer.problemRepository.save(Problem.builder()
                                .id(1L)
                                .content("content abc")
                                .number(1)
                                .exam(physicsExam1)
                                .build());

                physicsProblem2 = testContainer.problemRepository.save(Problem.builder()
                                .id(2L)
                                .content("content zxc")
                                .number(2)
                                .exam(physicsExam1)
                                .build());

                choice1 = testContainer.choiceRepository.save(Choice.builder()
                                .id(1L)
                                .content("choice 1")
                                .number(1)
                                .isAnswer(true)
                                .problem(physicsProblem1)
                                .build());

                choice2 = testContainer.choiceRepository.save(Choice.builder()
                                .id(2L)
                                .content("choice 2")
                                .number(2)
                                .isAnswer(false)
                                .problem(physicsProblem1)
                                .build());

                choice3 = testContainer.choiceRepository.save(Choice.builder()
                                .id(3L)
                                .content("choice 3")
                                .number(3)
                                .isAnswer(false)
                                .problem(physicsProblem1)
                                .build());
        }
}
