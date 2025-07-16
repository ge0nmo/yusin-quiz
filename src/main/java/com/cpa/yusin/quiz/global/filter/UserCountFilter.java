package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.visitor.service.VisitorService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Order(2)
@Component
@Slf4j
public class UserCountFilter extends OncePerRequestFilter
{
    private final VisitorService visitorService;

    List<String> WHITE_LIST = List.of("/css", "/js", "/favicon.ico", "/images");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException
    {
        try{
            visitorService.saveInRedis(request);
        } catch (Exception e){
            log.info("Failed to saveInRedis visitor count: {}", e.getMessage());
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException
    {
        String path = request.getRequestURI();

        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }
}
