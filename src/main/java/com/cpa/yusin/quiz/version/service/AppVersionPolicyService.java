package com.cpa.yusin.quiz.version.service;

import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.cpa.yusin.quiz.version.config.AppVersionPolicyProperties;
import com.cpa.yusin.quiz.version.controller.dto.AppVersionPolicyResponse;
import com.cpa.yusin.quiz.version.domain.AppPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionPolicyService {
    private final AppVersionPolicyProperties properties;

    public AppVersionPolicyResponse getPolicy(String code, String platformValue) {
        QualificationExamCode qualificationCode = QualificationExamCode.from(code)
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
        AppPlatform platform = AppPlatform.from(platformValue)
                .orElseThrow(() -> new ContentException(ExceptionMessage.INVALID_DATA));
        AppVersionPolicyProperties.Policy policy = properties.find(qualificationCode, platform)
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
        return new AppVersionPolicyResponse(
                policy.getLatestVersion(), policy.getMinimumVersion(), policy.getStoreUrl());
    }
}
