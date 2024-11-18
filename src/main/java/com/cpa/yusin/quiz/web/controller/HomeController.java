package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.global.annotation.CurrentUser;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller
public class HomeController
{
    private final AuthenticationService authenticationService;
    private final MemberService memberService;

    @GetMapping("/login")
    public String login()
    {
        return "login";
    }


    @GetMapping("/home")
    public String home(Model model)
    {

        return "home";
    }

}
