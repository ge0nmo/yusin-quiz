package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.common.service.ProblemChoiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
public class ChoiceController
{
    private final ChoiceService choiceService;
    private final ProblemChoiceFacade problemChoiceFacade;

    @PostMapping("/choice")
    public ResponseEntity<Long> save(@RequestParam("problemId") long problemId, @Validated @RequestBody ChoiceCreateRequest request)
    {
        long response = problemChoiceFacade.saveChoice(problemId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/choice/{choiceId}")
    public void update(@PathVariable("choiceId") long choiceId, @Validated @RequestBody ChoiceUpdateRequest request)
    {
        choiceService.update(choiceId, request);
    }

    @DeleteMapping("/choice/{choiceId}")
    public ResponseEntity<Void> delete(@PathVariable("choiceId") long choiceId)
    {
        choiceService.deleteById(choiceId);

        return ResponseEntity.noContent().build();
    }
}
