package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ChoiceServiceImpl implements ChoiceService
{
    private final ChoiceRepository choiceRepository;
}
