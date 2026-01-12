package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.filter.SecurityFilter;
import com.cpa.yusin.quiz.global.security.FormAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.cpa.yusin.quiz.global.utils.ApplicationConstants.FORM_ENDPOINT_WHITELIST;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    private final MemberDetailsService memberDetailsService;
    private final SecurityFilter securityFilter;

    public SecurityConfig(MemberDetailsService memberDetailsService,
                          SecurityFilter securityFilter)
    {
        this.memberDetailsService = memberDetailsService;
        this.securityFilter = securityFilter;
    }

    // =================================================================================
    // 1. 사용자 앱 API (기존 유지) : /api/v1/**
    // =================================================================================
    @Order(1)
    @Bean
    public SecurityFilterChain restApiSecurityFilter(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers((headers) ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용하므로 Stateless

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());

        http.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =================================================================================
    // 2. [NEW] Next.js 관리자 API : /api/admin/** (Stateless, JWT)
    // =================================================================================
    @Order(2)
    @Bean
    public SecurityFilterChain nextJsAdminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/admin/**") // 이 경로로 들어오는 요청만 처리
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 사용 안 함
                .logout(AbstractHttpConfigurer::disable)    // 세션 로그아웃 사용 안 함

                .headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // 프론트(3000) -> 백엔드(8080) 통신을 위한 CORS 허용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // JWT를 쓰므로 세션을 생성하지 않음 (Stateless)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/login").permitAll() // 로그인 API는 인증 없이 접근 가능
                        .anyRequest().hasRole("ADMIN") // 그 외 모든 관리자 API는 ADMIN 권한 필수
                );

        // JWT 인증 필터 추가
        http.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =================================================================================
    // Common Beans
    // =================================================================================

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        // JWT 관련 헤더 노출
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.addAllowedOrigin("*"); // 실제 배포 시에는 프론트엔드 도메인으로 제한하는 것 권장

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public FormAuthenticationProvider formAuthenticationProvider() {
        return new FormAuthenticationProvider(memberDetailsService, bCryptPasswordEncoder());
    }
}