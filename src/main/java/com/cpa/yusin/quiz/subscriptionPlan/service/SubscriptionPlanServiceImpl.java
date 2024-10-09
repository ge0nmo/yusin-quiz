package com.cpa.yusin.quiz.subscriptionPlan.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanRegisterRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanUpdateRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanRegisterResponse;
import com.cpa.yusin.quiz.subscriptionPlan.controller.port.SubscriptionPlanService;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import com.cpa.yusin.quiz.subscriptionPlan.controller.mapper.SubscriptionPlanMapper;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanRepository;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService
{
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final SubscriptionPlanValidator subscriptionPlanValidator;

    @Transactional
    @Override
    public SubscriptionPlanRegisterResponse save(SubscriptionPlanRegisterRequest request)
    {
        subscriptionPlanValidator.validateDurationMonth(request.getDurationMonth());

        SubscriptionPlan subscriptionPlan = subscriptionPlanMapper.toProductDomain(request);

        subscriptionPlan = subscriptionPlanRepository.save(subscriptionPlan);
        return subscriptionPlanMapper.toProductRegisterResponse(subscriptionPlan);
    }

    @Transactional
    @Override
    public void update(long productId, SubscriptionPlanUpdateRequest request)
    {
        SubscriptionPlan subscriptionPlan = findById(productId);
        subscriptionPlanValidator.validateDurationMonth(productId, request.getDurationMonth());

        subscriptionPlan.update(request);
        subscriptionPlanRepository.save(subscriptionPlan);
    }

    @Override
    public SubscriptionPlan findById(Long id)
    {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.PRODUCT_NOT_FOUND));
    }

    @Override
    public SubscriptionPlanDTO getById(Long id)
    {
        return subscriptionPlanMapper.toProductDTO(findById(id));
    }

    @Override
    public List<SubscriptionPlanDTO> getAll()
    {
        return subscriptionPlanRepository.findAll().stream()
                .map(subscriptionPlanMapper::toProductDTO)
                .sorted(Comparator.comparing(SubscriptionPlanDTO::getDurationMonth))
                .toList();
    }

    @Transactional
    @Override
    public void deleteById(Long id)
    {
        findById(id);

        subscriptionPlanRepository.deleteById(id);
    }


}
