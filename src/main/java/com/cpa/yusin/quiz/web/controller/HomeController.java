package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Slf4j
@Controller
public class HomeController
{
    private final AuthenticationService authenticationService;

    @GetMapping("/admin/login")
    public String home()
    {
        return "login";
    }

}
