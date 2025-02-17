package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import com.cpa.yusin.quiz.question.domain.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/question")
@Controller("webAnswerController")
public class AnswerController {
    private final AnswerService answerService;
    private final QuestionService questionService;
    private final ChoiceService choiceService;

    @GetMapping("/{questionId}/answer")
    public String getPage(@PathVariable Long questionId,
                          Model model)
    {
        Question question = questionService.findById(questionId);
        Problem problem = question.getProblem();

        List<Choice> choices = choiceService.findAllByProblemId(problem.getId());

        model.addAttribute("problem", problem);
        model.addAttribute("choices", choices);
        model.addAttribute("question", question);

        return "answer";
    }

    @ResponseBody
    @GetMapping("/{questionId}/answer/list")
    public ResponseEntity<?> getAnswers(@PathVariable("questionId") long questionId,
                                        @PageableDefault Pageable pageable)
    {
        Page<AnswerDTO> response = answerService.getAnswersByQuestionId(questionId, pageable);

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }


}
