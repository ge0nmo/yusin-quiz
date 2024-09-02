package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ChoiceServiceImpl implements ChoiceService
{
    private final ChoiceRepository choiceRepository;
    private final ChoiceMapper choiceMapper;

    @Override
    public List<ChoiceCreateResponse> save(List<ChoiceCreateRequest> choiceCreateRequests, ProblemDomain problem)
    {
        List<ChoiceDomain> choiceDomains = choiceMapper.fromCreateRequestToDomain(choiceCreateRequests, problem);

        choiceDomains = choiceRepository.saveAll(choiceDomains);

        return choiceMapper.toCreateResponses(choiceDomains);

    }

    @Override
    public void update(Map<Long, List<ChoiceUpdateRequest>> choiceUpdateMaps)
    {
        List<ChoiceDomain> domainsToUpdate = new ArrayList<>();
        List<Long> choiceIdsToDelete = new ArrayList<>();

        for (Map.Entry<Long, List<ChoiceUpdateRequest>> entry : choiceUpdateMaps.entrySet()) {
            long problemId = entry.getKey();
            List<ChoiceUpdateRequest> requests = entry.getValue();

            for(ChoiceUpdateRequest request : requests) {
                if(request.isDeleted()){
                    choiceIdsToDelete.add(request.getId());
                } else{
                    ChoiceDomain choiceDomain = findById(request.getId());
                    choiceDomain.update(problemId, request);
                    domainsToUpdate.add(choiceDomain);
                }
            }
        }

        deleteProcess(choiceIdsToDelete);
        updateProcess(domainsToUpdate);
    }

    private void updateProcess(List<ChoiceDomain> domainsToUpdate)
    {
        if(!domainsToUpdate.isEmpty()){
            choiceRepository.saveAll(domainsToUpdate);
        }
    }

    private void deleteProcess(List<Long> choiceIdsToDelete)
    {
        if(!choiceIdsToDelete.isEmpty()){
            choiceRepository.deleteAllByIdInBatch(choiceIdsToDelete);
        }
    }

    @Override
    public ChoiceDomain findById(long id)
    {
        return choiceRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.CHOICE_NOT_FOUND));
    }

    @Override
    public List<ChoiceDomain> findAllByProblemId(long problemId)
    {
        return choiceRepository.findAllByProblemId(problemId);
    }

    @Override
    public Map<Long, List<ChoiceResponse>> findAllByExamId(long examId)
    {
        List<ChoiceDomain> choices = choiceRepository.findAllByExamId(examId);

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
                .map(ChoiceDomain::getId)
                .toList();

        choiceRepository.deleteAllByIdInBatch(choiceList);
    }

    @Override
    public void deleteAllByProblemIds(List<Long> problemIds)
    {
        List<Long> choiceList = choiceRepository.findAllByProblemIds(problemIds).stream()
                .map(ChoiceDomain::getId)
                .toList();

        choiceRepository.deleteAllByIdInBatch(choiceList);
    }
}
