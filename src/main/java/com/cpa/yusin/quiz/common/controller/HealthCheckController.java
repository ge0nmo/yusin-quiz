package com.cpa.yusin.quiz.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

@RequestMapping("/api/v1")
@RestController
public class HealthCheckController {
    @Value("${server.env:local}")
    private String env;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.serverAddress:localhost}")
    private String serverAddress;

    @Value("${serverName:yusin-quiz}")
    private String serverName;

    @GetMapping(value = "/hc", produces = "application/json")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> responseData = new TreeMap<>();
        responseData.put("serverName", serverName);
        responseData.put("serverAddress", serverAddress);
        responseData.put("serverPort", serverPort);
        responseData.put("env", env);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping(value = "/env", produces = "application/json")
    public ResponseEntity<?> env() {
        return ResponseEntity.ok(env);
    }

}
