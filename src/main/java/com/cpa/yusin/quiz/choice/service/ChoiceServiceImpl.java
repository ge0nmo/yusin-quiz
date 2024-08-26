package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
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
    public void update(long problemId, List<ChoiceUpdateRequest> requests)
    {
        List<ChoiceDomain> domains = new ArrayList<>();
        for(ChoiceUpdateRequest request : requests)
        {
            if(request.isDeleted()){
                choiceRepository.deleteById(request.getId());
                continue;
            }
            ChoiceDomain choiceDomain = findById(request.getId());
            choiceDomain = choiceDomain.update(problemId, request);
            domains.add(choiceDomain);
        }

        choiceRepository.saveAll(domains);
    }

    @Override
    public ChoiceDomain findById(long id)
    {
        return choiceRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.CHOICE_NOT_FOUND));
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
