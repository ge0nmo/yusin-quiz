package com.cpa.yusin.quiz.global.controller;

import com.cpa.yusin.quiz.global.init.ProblemMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMigrationController
{
    private final ProblemMigrationService migrationService;

    @PostMapping("/migrate/problem-html")
    public ResponseEntity<String> triggerMigration()
    {
        String result = migrationService.migrateHtmlToJson();
        return ResponseEntity.ok(result);
    }
}