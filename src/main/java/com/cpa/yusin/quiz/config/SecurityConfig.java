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
public class SecurityConfig
{
    private final CustomOAuth2Service oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final MemberDetailsService memberDetailsService;
    private final SecurityFilter securityFilter;

    public SecurityConfig(CustomOAuth2Service oAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                          HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository,
                          MemberDetailsService memberDetailsService, SecurityFilter securityFilter)
    {
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
        this.memberDetailsService = memberDetailsService;
        this.securityFilter = securityFilter;
    }


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
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        /*.requestMatchers(API_ENDPOINT_WHITELIST
                        ).permitAll()*/
                        .anyRequest().permitAll());

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
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Order(2)
    @Bean("formLoginSecurityFilterChain")
    public SecurityFilterChain formLoginSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers((headers) ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(FORM_ENDPOINT_WHITELIST
                        ).permitAll()

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
                .logout(logout ->
                        logout
                                .logoutSuccessUrl("/admin/login")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                                .logoutSuccessUrl("/admin/login"))
        ;

        http.authenticationProvider(formAuthenticationProvider());

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));

        configuration.addAllowedOrigin("*");

        //configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public FormAuthenticationProvider formAuthenticationProvider() {
        return new FormAuthenticationProvider(memberDetailsService, bCryptPasswordEncoder());
    }
}
