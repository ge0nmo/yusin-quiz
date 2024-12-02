package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
public class ChoiceController
{
    private final ChoiceService choiceService;


    @PatchMapping("/choice/{choiceId}")
    public void update(@PathVariable("choiceId") long choiceId, @Validated @RequestBody ChoiceUpdateRequest request)
    {
        choiceService.update(choiceId, request);
    }
}
