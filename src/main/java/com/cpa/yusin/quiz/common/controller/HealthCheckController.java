package com.cpa.yusin.quiz.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

@RestController
public class HealthCheckController
{
    @Value("${server.env}")
    private String env;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${serverName}")
    private String serverName;

    @GetMapping(value = "/hc", produces = "application/json")
    public ResponseEntity<?> healthCheck(){
        Map<String, String> responseData = new TreeMap<>();
        responseData.put("serverName", serverName);
        responseData.put("serverAddress", serverAddress);
        responseData.put("serverPort", serverPort);
        responseData.put("env", env);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping(value = "/env", produces = "application/json")
    public ResponseEntity<?> env()
    {
        return ResponseEntity.ok(env);
    }

}
