package com.cpa.yusin.quiz.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

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
    public String home(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }



        model.addAttribute("activePage", "home");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "home";
    }



}
