package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.controller.port.CreateProblemV2Service;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateProblemV2ServiceImpl implements CreateProblemV2Service
{
    private final ProblemRepository problemRepository;
    private final ExamService examService;
    private final ChoiceService choiceService;
    private final ProblemValidator problemValidator;
    private final ProblemNumberSlotManager problemNumberSlotManager;
    private final YoutubeLectureUrlProcessor youtubeLectureUrlProcessor;

    @Override
    public void saveOrUpdateV2(long examId, ProblemSaveV2Request request)
    {
        if (request.isNew()) {
            Exam exam = examService.findById(examId);
            problemValidator.validateCreateNumber(examId, request.getNumber());
            problemNumberSlotManager.releaseRemovedNumberSlot(examId, request.getNumber());

            Problem problem = Problem.fromSaveOrUpdate(
                    request.getContent(),
                    request.getExplanation(),
                    request.getNumber(),
                    exam
            );
            applyLecture(problem, request);

            problemRepository.save(problem);
            choiceService.saveOrUpdate(request.getChoices(), problem);

            log.info("V2 Created Problem: ID={}", problem.getId());
        }
        else {
            examService.findById(examId);
            Problem problem = problemRepository.findById(request.getId())
                    .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));
            problemValidator.validateBelongsToExam(problem, examId);
            problemValidator.validateUpdateNumber(problem, request.getNumber());
            problemNumberSlotManager.releaseRemovedNumberSlot(examId, request.getNumber());

            problem.update(
                    request.getContent(),
                    request.getNumber(),
                    request.getExplanation()
            );
            applyLecture(problem, request);

            choiceService.saveOrUpdate(request.getChoices(), problem);

            log.info("V2 Updated Problem: ID={}", problem.getId());
        }
    }

    private void applyLecture(Problem problem, ProblemSaveV2Request request) {
        if (request.getLecture() == null) {
            problem.clearLecture();
            return;
        }

        YoutubeLectureUrlProcessor.NormalizedYoutubeLecture normalizedYoutubeLecture =
                youtubeLectureUrlProcessor.normalize(request.getLecture());

        problem.assignLecture(
                normalizedYoutubeLecture.canonicalYoutubeUrl(),
                normalizedYoutubeLecture.startTimeSecond()
        );
    }
}
