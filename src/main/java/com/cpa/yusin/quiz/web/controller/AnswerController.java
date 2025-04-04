package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.web.dto.AdminAnswerRegisterRequest;
import com.cpa.yusin.quiz.web.dto.AdminAnswerUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller("webAnswerController")
public class AnswerController {
    private final AnswerService answerService;
    private final QuestionService questionService;
    private final ChoiceService choiceService;

    @GetMapping("/question/{questionId}/answer")
    public String getPage(@PathVariable Long questionId,
                          Model model)
    {
        Question question = questionService.findById(questionId);
        Problem problem = question.getProblem();

        List<Choice> choices = choiceService.findAllByProblemId(problem.getId());

        List<AnswerDTO> answers = answerService.getAnswersByQuestionId(questionId);

        model.addAttribute("problem", problem);
        model.addAttribute("choices", choices);
        model.addAttribute("question", question);
        model.addAttribute("answers", answers);

        return "answer";
    }

    @ResponseBody
    @PostMapping("/question/{questionId}/answer")
    public ResponseEntity<?> createAnswer(@PathVariable("questionId") long questionId,
                                          @Validated @RequestBody AdminAnswerRegisterRequest request,
                                          Principal principal)
    {
        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();

        long response = answerService.save(request, questionId, memberDetails.getMember());

        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @PatchMapping("/answer/{answerId}")
    public ResponseEntity<?> updateAnswer(@PathVariable("answerId") long answerId,
                                          @Validated @RequestBody AdminAnswerUpdateRequest request,
                                          Principal principal)
    {
        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();

        answerService.updateInAdminPage(request, answerId);
        AnswerDTO response = answerService.getAnswerById(answerId);
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @DeleteMapping("/answer/{answerId}")
    public void deleteAnswer(@PathVariable("answerId") long answerId)
    {
        answerService.deleteAnswer(answerId);
    }
}
