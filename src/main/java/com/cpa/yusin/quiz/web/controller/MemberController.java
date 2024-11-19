package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        Page<Member> response = memberService.getAllAdminNot(null, PageRequest.of(0, 1));
        log.info("조회 완료");

        model.addAttribute("members", response);

        return "member";
    }

}
