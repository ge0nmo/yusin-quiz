package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.filter.SecurityFilter;
import com.cpa.yusin.quiz.global.security.FormAuthenticationProvider;
import com.cpa.yusin.quiz.global.security.oauth2.CustomOAuth2Service;
import com.cpa.yusin.quiz.global.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.cpa.yusin.quiz.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
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
public class SecurityConfig {

    private final CustomOAuth2Service oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final MemberDetailsService memberDetailsService;
    private final SecurityFilter securityFilter; // JWT 인증 필터

    public SecurityConfig(CustomOAuth2Service oAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                          HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository,
                          MemberDetailsService memberDetailsService,
                          SecurityFilter securityFilter) {
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
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
                        .anyRequest().permitAll()); // 세부 권한은 컨트롤러나 서비스에서 체크한다고 가정

        http
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/api/v1/oauth2/*")
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/oauth2/callback"))
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(oAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

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
    // 3. 기존 타임리프 관리자 페이지 : /admin/** (Session, Form Login)
    // =================================================================================
    @Order(3)
    @Bean("formLoginSecurityFilterChain")
    public SecurityFilterChain formLoginSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**") // /api/admin을 제외한 나머지 /admin 경로
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers((headers) ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(FORM_ENDPOINT_WHITELIST).permitAll()
                        .anyRequest().authenticated());

        http
                .formLogin(login ->
                        login
                                .loginPage("/admin/login")
                                .loginProcessingUrl("/admin/login")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .successForwardUrl("/admin/home")
                                .defaultSuccessUrl("/admin/home", true)
                                .failureUrl("/admin/login?error")
                                .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(86400 * 14)
                        .userDetailsService(memberDetailsService)
                        .authenticationSuccessHandler((request, response, authentication) -> {
                            response.sendRedirect("/admin/home");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        // 기존 방식 유지를 위해 FormAuthenticationProvider 사용
        http.authenticationProvider(formAuthenticationProvider());

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