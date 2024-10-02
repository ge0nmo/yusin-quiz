package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.filter.SecurityFilter;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.security.oauth2.CustomOAuth2Service;
import com.cpa.yusin.quiz.global.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.cpa.yusin.quiz.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    private final CustomOAuth2Service oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final JwtService jwtService;
    private final MemberDetailsService memberDetailsService;

    public SecurityConfig(CustomOAuth2Service oAuth2UserService, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository, JwtService jwtService, MemberDetailsService memberDetailsService)
    {
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
        this.jwtService = jwtService;
        this.memberDetailsService = memberDetailsService;
    }

    @Bean
    public SecurityFilter securityFilter()
    {
        return new SecurityFilter(memberDetailsService, jwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/home/login",
                                "/api/v1/oauth2/**",
                                "/api/v1/sign-up",
                                "/api/v1/file/**",
                                "/api/v1/login").permitAll()

                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated());

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

        http
                .addFilterBefore(securityFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
