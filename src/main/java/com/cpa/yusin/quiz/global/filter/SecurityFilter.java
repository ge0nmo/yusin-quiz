package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.StringUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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
        final String header = getAuthorizationHeader(request);

        if(!isValidHeader(header)){
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String email = jwtService.extractSubject(token);

        if(!StringUtils.hasText(email) || SecurityContextHolder.getContext().getAuthentication() != null){
            filterChain.doFilter(request, response);
        }

        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(email);

        if(!jwtService.validateToken(token, memberDetails)){
            filterChain.doFilter(request, response);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

    private boolean isValidHeader(String header) {
        return StringUtils.hasText(header) && header.startsWith("Bearer ");
    }

    private String getAuthorizationHeader(HttpServletRequest request)
    {
        return request.getHeader(AUTHORIZATION);
    }
}
