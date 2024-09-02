package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
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
    public void saveOrUpdate(Map<ProblemDomain, List<ChoiceRequest>> choiceMaps)
    {
        List<ChoiceDomain> saveOrUpdate = new ArrayList<>();
        List<Long> deleteList = new ArrayList<>();

        for(Map.Entry<ProblemDomain, List<ChoiceRequest>> entry : choiceMaps.entrySet()) {
            ProblemDomain problem = entry.getKey();
            List<ChoiceRequest> requests = entry.getValue();

            for(ChoiceRequest request : requests) {
                if(request.isDeleted() && !request.isNew()){
                    deleteList.add(request.getId());
                } else{
                    ChoiceDomain choiceDomain;
                    if(request.isNew()) {
                        choiceDomain = choiceMapper.fromCreateRequestToDomain(request, problem);
                    } else{
                        choiceDomain = findById(request.getId());
                        choiceDomain.update(problem.getId(), request);
                    }
                    saveOrUpdate.add(choiceDomain);
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

    private void saveProcess(List<ChoiceDomain> domains)
    {
        if(!domains.isEmpty()){
            choiceRepository.saveAll(domains);
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
