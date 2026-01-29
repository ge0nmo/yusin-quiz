package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.controller.port.CreateProblemV2Service;
import com.cpa.yusin.quiz.problem.controller.port.GetProblemV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/admin/problem")
@RestController
public class AdminProblemV2Controller
{
    private final CreateProblemV2Service createProblemV2Service;
    private final GetProblemV2Service getProblemV2Service;

    /**
     * [V2] JSON 기반 문제 생성 및 수정 (통합 엔드포인트)
     * 프론트엔드 Tiptap JSON 저장 시 이 API를 호출합니다.
     */
    @PostMapping
    public ResponseEntity<GlobalResponse<String>> saveOrUpdateV2(@RequestParam("examId") long examId,
                                                                 @Validated @RequestBody ProblemSaveV2Request request
    ) {
        createProblemV2Service.saveOrUpdateV2(examId, request);
        return ResponseEntity.ok(new GlobalResponse<>("Saved Successfully (V2)"));
    }


    // [New] 단건 조회
    @GetMapping("/{problemId}")
    public ResponseEntity<GlobalResponse<ProblemV2Response>> getById(@PathVariable Long problemId)
    {
        ProblemV2Response response = getProblemV2Service.getById(problemId);
        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    // [New] 시험별 전체 조회
    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProblemV2Response>>> getAllByExamId(@RequestParam Long examId)
    {
        List<ProblemV2Response> response = getProblemV2Service.getAllByExamId(examId);
        return ResponseEntity.ok(new GlobalResponse<>(response));
    }
}
