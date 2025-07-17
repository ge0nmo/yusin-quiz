package com.cpa.yusin.quiz.global.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VisitorWhiteListMatcher
{
    public static final List<String> PATH_WHITELIST = List.of("/css", "/js", "/favicon.ico", "/images");
    public static final List<String> AGENT_WHITELIST = List.of(
            // Common tools/scripts
            "curl", "wget", "http.client", "python-requests", "okhttp", "apachehttpclient",
            "java/", "go-http-client", "zgrab", "custom-asynchttpclient",

            // Monitoring/CI/CD
            "prometheus", "grafana", "newrelic", "datadog",
            "github", "gitlab", "jenkins", "circleci",

            // Security scanners
            "nmap", "nessus", "openvas", "nikto", "acunetix",
            "qualys", "netsparker", "burp", "owasp",

            // Bots/Crawlers (broad coverage)
            "bot", "crawler", "spider", "crawl", "slurp", "search",
            "index", "scanner", "monitor", "probe",

            "hookshot", "httpclient", "async", "monitoring", "python"
    );

    public boolean isWhiteListed(String uri, String userAgent)
    {
        return isWhiteListPath(uri) || isWhiteListAgent(userAgent);
    }

    private boolean isWhiteListPath(String uri)
    {
        return PATH_WHITELIST.stream().anyMatch(uri::startsWith);
    }

    private boolean isWhiteListAgent(String agent)
    {
        return AGENT_WHITELIST.stream().anyMatch(agent::contains);
    }
}

