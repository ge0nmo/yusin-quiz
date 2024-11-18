package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.member.controller.port.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller("webMemberController")
public class MemberController
{
    private final MemberService memberService;

    @GetMapping("/member")
    public String member(Model model)
    {
        model.addAttribute("activePage", "member");
        return "member";
    }
}
