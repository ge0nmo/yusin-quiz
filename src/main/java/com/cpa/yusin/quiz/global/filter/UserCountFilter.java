package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.global.utils.VisitorWhiteListMatcher;
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
import java.util.Optional;

@RequiredArgsConstructor
@Order(2)
@Component
@Slf4j
public class UserCountFilter extends OncePerRequestFilter
{
    private final VisitorService visitorService;
    private final VisitorWhiteListMatcher whiteListMatcher;

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
        String requestURI = request.getRequestURI();
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("").toLowerCase();

        return whiteListMatcher.isWhiteListed(requestURI, userAgent);
    }

}
