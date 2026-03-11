package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/problem")
@RestController
public class ProblemController
{
    private final ProblemService problemService;

    /**
     * V1 문제 상세 조회 API.
     *
     * 레거시 HTML 렌더링 화면도 lecture 객체를 동일하게 받도록 유지해야 함.
     * 프론트는 아래 규칙만 따르면 됨:
     * - lecture = null 이면 해설강의 미연결 상태
     * - lecture.youtubeUrl 은 canonical watch URL
     * - lecture.playbackUrl 은 startTimeSecond 가 있으면 즉시 재생 가능한 링크
     */
    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ProblemDTO>> getById(@PathVariable("id") long id)
    {
        ProblemDTO response = problemService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    /**
     * 시험별 V1 문제 목록 조회 API.
     *
     * 문제 카드 목록에서 해설강의 배지/버튼을 바로 그릴 수 있게
     * 각 문제의 lecture 정보를 함께 내려야 함.
     */
    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProblemDTO>>> getAllByExamId(@RequestParam("examId") long examId)
    {
        GlobalResponse<List<ProblemDTO>> response = problemService.getAllByExamId(examId);

        return ResponseEntity.ok(response);
    }

}
