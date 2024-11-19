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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller("webMemberController")
public class MemberController
{
    private final MemberService memberService;

    @GetMapping("/member")
    public String member(Model model, @RequestParam("page")Optional<Integer> page, @RequestParam("size") Optional<Integer> size)
    {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        Page<Member> response = memberService.getAllAdminNot(null, PageRequest.of(currentPage - 1, pageSize));

        model.addAttribute("members", response);

        int totalPages = response.getTotalPages();
        if(totalPages > 0){
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .toList();
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "member";
    }

}
