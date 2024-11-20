package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
public class MemberController
{
    private final MemberService memberService;

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<MemberDTO>> getById(@PathVariable("id") long id)
    {
        MemberDTO response = memberService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<MemberDTO>>> getMembers(@RequestParam(value = "keyword", required = false) String keyword,
                                                                      @PageableDefault Pageable pageable)
    {
        Page<MemberDTO> response = memberService.getAll(keyword, pageable.previousOrFirst());

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }
}
