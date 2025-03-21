package com.cpa.yusin.quiz.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller
public class HomeController
{
    @GetMapping("/login")
    public String login()
    {
        return "login";
    }


    @GetMapping("/home")
    public String home(Model model)
    {
        model.addAttribute("activePage", "home");
        return "home";
    }

}
