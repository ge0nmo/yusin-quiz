package com.cpa.yusin.quiz.web.controller;


import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/question")
@Controller("webQuestionController")
public class QuestionController
{
    private final QuestionService questionService;


    @GetMapping()
    public String getQuestionPage()
    {
        return "question";
    }

    @ResponseBody
    @GetMapping("/list")
    public ResponseEntity<?> getQuestions(@PageableDefault Pageable pageable)
    {
        Page<QuestionDTO> response = questionService.findAllQuestions(pageable);

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }

}
