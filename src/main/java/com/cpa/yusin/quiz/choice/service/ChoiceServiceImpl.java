package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
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
import java.util.Collections;
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

    @Transactional
    @Override
    public void save(Problem problem, List<ChoiceCreateRequest> requests)
    {
        List<Choice> choiceRequests = requests.stream().map(request -> choiceMapper.fromCreateRequestToChoice(request, problem))
                .toList();

        choiceRepository.saveAll(choiceRequests);
    }

    @Transactional
    @Override
    public long save(Choice choice)
    {
        return choiceRepository.save(choice).getId();
    }

    @Transactional
    public List<Choice> saveOrUpdate(List<ChoiceRequest> requests, Problem problem)
    {
        List<Choice> choices = new ArrayList<>();
        for(ChoiceRequest request : requests)
        {
            Choice choice = request.isNew() ? Choice.fromSaveOrUpdate(request, problem) : update(request);
            if(choice != null) choices.add(choice);
        }

        if(!choices.isEmpty())
            return choiceRepository.saveAll(choices);

        return Collections.emptyList();
    }

    private Choice update(ChoiceRequest request)
    {
        Choice choice = findById(request.getId());
        if(request.isRemovedYn()){
            choiceRepository.deleteById(choice.getId());
            return null;
        }

        choice.update(request.getNumber(), request.getContent(), request.getIsAnswer());
        return choice;
    }

    @Transactional
    @Override
    public void update(List<ChoiceUpdateRequest> requests, Problem problem)
    {
        for(ChoiceUpdateRequest request : requests)
        {
            if(request.getId() == null){
                Choice choice = choiceMapper.fromUpdateRequestToChoice(request, problem);
                choiceRepository.save(choice);
            } else{
                Choice choice = findById(request.getId());
                if(request.getIsDeleted()){
                    choiceRepository.deleteById(choice.getId());
                }else{
                    choice.update(request.getNumber(), request.getContent(), request.getIsAnswer());
                    choiceRepository.save(choice);
                }
            }
        }
    }

    @Transactional
    @Override
    public void update(long choiceId, ChoiceUpdateRequest request)
    {
        Choice choice = findById(choiceId);

        choice.update(request.getNumber(), request.getContent(), request.getIsAnswer());

        choiceRepository.save(choice);
    }

    @Transactional
    @Override
    public void deleteById(long choiceId)
    {
        Choice choice = findById(choiceId);

        choiceRepository.deleteById(choice.getId());
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
