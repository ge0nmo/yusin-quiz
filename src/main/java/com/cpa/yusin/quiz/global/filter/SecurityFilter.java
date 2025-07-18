package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Order(1)
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter
{
    private final MemberDetailsService memberDetailsService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException
    {
        String token = getAuthorizationHeaderOrCookie(request);

        if(token == null){
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractSubject(token);

        if(!StringUtils.hasText(email) || SecurityContextHolder.getContext().getAuthentication() != null){
            filterChain.doFilter(request, response);
        }

        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(email);

        if(!jwtService.isValidToken(token, memberDetails)){
            filterChain.doFilter(request, response);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }

    private boolean isValidHeader(String header) {
        return StringUtils.hasText(header) && header.startsWith("Bearer ");
    }

    private String getAuthorizationHeaderOrCookie(HttpServletRequest request)
    {
        String headerToken = request.getHeader(AUTHORIZATION);
        if(isValidHeader(headerToken)){
            return headerToken.substring(7);
        }
        Cookie[] cookies = request.getCookies();

        if(cookies != null){
            for(Cookie cookie : cookies){
                if("JWT_TOKEN".equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
