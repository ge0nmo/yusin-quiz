package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.global.exception.ChoiceException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.problem.domain.Problem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ChoiceServiceImpl implements ChoiceService
{
    private final ChoiceRepository choiceRepository;
    private final ChoiceMapper choiceMapper;


    @Override
    public void saveOrUpdate(Map<Problem, List<ChoiceRequest>> choiceMaps)
    {
        List<Choice> saveOrUpdate = new ArrayList<>();
        List<Long> deleteList = new ArrayList<>();

        for(Map.Entry<Problem, List<ChoiceRequest>> entry : choiceMaps.entrySet()) {
            Problem problem = entry.getKey();
            List<ChoiceRequest> requests = entry.getValue();

            for(ChoiceRequest request : requests) {
                if(Boolean.TRUE.equals(request.getIsDeleted()) && !request.isNew()){
                    deleteList.add(request.getId());
                } else{
                    Choice choice;
                    if(request.isNew()) {
                        choice = choiceMapper.fromCreateRequestToDomain(request, problem);
                    } else{
                        choice = findById(request.getId());
                        log.info("request = {}", request.toString());
                        choice.update(problem.getId(), request);
                    }
                    saveOrUpdate.add(choice);
                }
            }
        }

        deleteProcess(deleteList);
        saveProcess(saveOrUpdate);
    }

    private void deleteProcess(List<Long> choiceIdsToDelete)
    {
        if(!choiceIdsToDelete.isEmpty()){
            choiceRepository.deleteAllByIdInBatch(choiceIdsToDelete);
        }
    }

    private void saveProcess(List<Choice> domains)
    {
        if(!domains.isEmpty()){
            choiceRepository.saveAll(domains);
        }
    }

    @Override
    public Choice findById(long id)
    {
        return choiceRepository.findById(id)
                .orElseThrow(() -> new ChoiceException(ExceptionMessage.CHOICE_NOT_FOUND));
    }

    @Override
    public List<Choice> findAllByProblemId(long problemId)
    {
        return choiceRepository.findAllByProblemId(problemId);
    }

    @Override
    public List<ChoiceResponse> getAllByProblemId(long problemId)
    {
        List<Choice> choices = findAllByProblemId(problemId);

        return choiceMapper.toResponses(choices);
    }

    @Override
    public Map<Long, List<ChoiceResponse>> findAllByExamId(long examId)
    {
        List<Choice> choices = choiceRepository.findAllByExamId(examId);

        return choices.stream()
                .collect(groupingBy(
                        choice -> choice.getProblem().getId(),
                        mapping(choiceMapper::toResponse, toList())));
    }

    @Override
    public void deleteAllByIds(List<Long> ids)
    {
        choiceRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteAllByProblemId(long problemId)
    {
        List<Long> choiceList = choiceRepository.findAllByProblemId(problemId).stream()
                .map(Choice::getId)
                .toList();

        choiceRepository.deleteAllByIdInBatch(choiceList);
    }

    @Override
    public void deleteAllByProblemIds(List<Long> problemIds)
    {
        List<Long> choiceList = choiceRepository.findAllByProblemIds(problemIds).stream()
                .map(Choice::getId)
                .toList();

        choiceRepository.deleteAllByIdInBatch(choiceList);
    }
}
