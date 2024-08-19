package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
public class MemberController
{
    private final MemberService memberService;

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<MemberDTO>> getById(@PathVariable("id") Long id)
    {
        MemberDTO response = memberService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }
}
