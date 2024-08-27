package com.cpa.yusin.quiz.common.infrastructure;

import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.service.CascadeDeleteService;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import org.springframework.stereotype.Service;

@Service
public class CascadeDeleteServiceImpl implements CascadeDeleteService
{
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;
    private final ProblemRepository problemRepository;
    private final ChoiceRepository choiceRepository;

    public CascadeDeleteServiceImpl(SubjectRepository subjectRepository, ExamRepository examRepository, ProblemRepository problemRepository, ChoiceRepository choiceRepository)
    {
        this.subjectRepository = subjectRepository;
        this.examRepository = examRepository;
        this.problemRepository = problemRepository;
        this.choiceRepository = choiceRepository;
    }


    @Override
    public void deleteSubjectBySubjectId(long subjectId)
    {
        choiceRepository.deleteAllBySubjectId(subjectId);
        problemRepository.deleteAllBySubjectId(subjectId);
        examRepository.deleteAllBySubjectId(subjectId);
        subjectRepository.deleteById(subjectId);
    }

    @Override
    public void deleteExamByExamId(long examId)
    {
        choiceRepository.deleteAllByExamId(examId);
        problemRepository.deleteAllByExamId(examId);

        examRepository.deleteById(examId);
    }
}
