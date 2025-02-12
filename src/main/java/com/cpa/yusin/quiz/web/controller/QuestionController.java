package com.cpa.yusin.quiz.web.controller;


import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/question")
@Controller("webQuestionController")
public class QuestionController
{
    private final QuestionService questionService;


    @GetMapping()
    public String getQuestionPage(Model model, @PageableDefault Pageable pageable)
    {
        Page<QuestionDTO> response = questionService.findAllQuestions(pageable.previousOrFirst());

        model.addAttribute("questions", response.getContent());
        model.addAttribute("pageInfo", response);

        return "question";
    }

}
