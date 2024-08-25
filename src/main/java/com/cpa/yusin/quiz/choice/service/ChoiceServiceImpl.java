package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
