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
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ChoiceServiceImpl implements ChoiceService {
    private final ChoiceRepository choiceRepository;
    private final ChoiceMapper choiceMapper;

    @Transactional
    @Override
    public void save(Problem problem, List<ChoiceCreateRequest> requests) {
        validateChoiceNumbers(requests.stream()
                .map(ChoiceCreateRequest::getNumber)
                .toList());

        List<Choice> choiceRequests = requests.stream()
                .map(request -> choiceMapper.fromCreateRequestToChoice(request, problem))
                .toList();

        choiceRepository.saveAll(choiceRequests);
    }

    @Transactional
    @Override
    public long save(Choice choice) {
        return choiceRepository.save(choice).getId();
    }

    @Transactional
    @Override
    public List<Choice> saveOrUpdate(List<ChoiceRequest> requests, Problem problem) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        validateChoiceNumbers(requests.stream()
                .filter(request -> !request.isRemovedYn())
                .map(ChoiceRequest::getNumber)
                .toList());

        List<Choice> choices = new ArrayList<>();
        for (ChoiceRequest request : requests) {
            if (request.isRemovedYn() && request.isNew()) {
                continue;
            }

            Choice choice = request.isNew()
                    ? Choice.fromSaveOrUpdate(request.getContent(), request.getNumber(), request.getIsAnswer(), problem)
                    : update(problem, request);
            if (choice != null) {
                choices.add(choice);
            }
        }

        if (!choices.isEmpty()) {
            return choiceRepository.saveAll(choices);
        }

        return Collections.emptyList();
    }

    private Choice update(Problem problem, ChoiceRequest request) {
        Choice choice = findById(request.getId());
        validateChoiceOwnership(choice, problem);

        if (request.isRemovedYn()) {
            choiceRepository.deleteById(choice.getId());
            return null;
        }

        choice.update(request.getNumber(), request.getContent(), request.getIsAnswer());
        return choice;
    }

    @Transactional
    @Override
    public void update(long choiceId, ChoiceUpdateRequest request) {
        Choice choice = findById(choiceId);

        choice.update(request.getNumber(), request.getContent(), request.getIsAnswer());

        choiceRepository.save(choice);
    }

    @Transactional
    @Override
    public void deleteById(long choiceId) {
        Choice choice = findById(choiceId);

        choiceRepository.deleteById(choice.getId());
    }

    @Override
    public Choice findById(long id) {
        return choiceRepository.findById(id)
                .orElseThrow(() -> new ChoiceException(ExceptionMessage.CHOICE_NOT_FOUND));
    }

    @Override
    public List<Choice> findAllByProblemId(long problemId) {
        return choiceRepository.findAllByProblemId(problemId);
    }

    @Override
    public List<ChoiceResponse> getAllByProblemId(long problemId) {
        List<Choice> choices = findAllByProblemId(problemId);

        return choiceMapper.toResponses(choices);
    }

    @Override
    public Map<Long, List<ChoiceResponse>> findAllByExamId(long examId) {
        List<Choice> choices = choiceRepository.findAllByExamId(examId);

        return choices.stream()
                .collect(groupingBy(
                        choice -> choice.getProblem().getId(),
                        mapping(choiceMapper::toResponse, toList())));
    }

    @Override
    public Map<Long, List<ChoiceResponse>> findAllByProblemIds(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Choice> choices = choiceRepository.findAllByProblemIds(problemIds);

        return choices.stream()
                .collect(groupingBy(
                        choice -> choice.getProblem().getId(),
                        mapping(choiceMapper::toResponse, toList())));
    }

    private void validateChoiceOwnership(Choice choice, Problem problem) {
        if (!choice.getProblem().getId().equals(problem.getId())) {
            throw new ChoiceException(ExceptionMessage.INVALID_DATA);
        }
    }

    private void validateChoiceNumbers(List<Integer> numbers) {
        Set<Integer> uniqueNumbers = new HashSet<>();
        for (Integer number : numbers) {
            if (!uniqueNumbers.add(number)) {
                throw new ChoiceException(ExceptionMessage.INVALID_DATA);
            }
        }
    }
}
