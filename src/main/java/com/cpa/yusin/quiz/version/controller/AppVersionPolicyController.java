package com.cpa.yusin.quiz.version.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.version.controller.dto.AppVersionPolicyResponse;
import com.cpa.yusin.quiz.version.service.AppVersionPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qualification-exams/{code}")
@RequiredArgsConstructor
public class AppVersionPolicyController {
    private final AppVersionPolicyService appVersionPolicyService;

    @GetMapping("/app-version-policy")
    public GlobalResponse<AppVersionPolicyResponse> getPolicy(@PathVariable String code,
                                                               @RequestParam String platform) {
        return GlobalResponse.success(appVersionPolicyService.getPolicy(code, platform));
    }
}
