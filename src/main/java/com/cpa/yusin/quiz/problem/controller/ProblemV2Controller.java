package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.controller.port.GetProblemV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v2/problem")
@RestController
public class ProblemV2Controller
{
    private final GetProblemV2Service getProblemV2Service;

    /**
     * 사용자 앱의 Block 기반 문제 목록 조회 API.
     *
     * 프론트는 각 문제의 lecture 객체를 보고
     * - 해설강의 버튼 노출 여부
     * - startTimeSecond 기반 딥링크 이동 여부
     * 를 즉시 판단해야 함.
     *
     * playbackUrl 을 함께 내려주므로 클라이언트는 유튜브 URL 조합 로직을 중복 구현하지 않아도 됨.
     */
    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProblemV2Response>>> getAllByExamId(@RequestParam("examId") long examId)
    {
        List<ProblemV2Response> response = getProblemV2Service.getAllByExamId(examId);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }

}
