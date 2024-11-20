package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.common.controller.dto.request.DataTableRequest;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller("webMemberController")
public class MemberController
{
    private final MemberService memberService;

    @GetMapping("/member")
    public String member(Model model, @ModelAttribute("params") DataTableRequest request)
    {
        model.addAttribute("activePage", "member");

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Member> response = memberService.getAllAdminNot(null, pageable);
        model.addAttribute("response", response);
        return "member";
    }

}
