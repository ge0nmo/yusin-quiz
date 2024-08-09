package com.cpa.yusin.quiz.security.oauth2;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler
{
    private final JwtService jwtService;
    private final Environment env;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException
    {
        log.info("onAuthenticationSuccess");
        String targetUri = determineTargetUri(authentication);
    }

    public String determineTargetUri(Authentication authentication)
    {
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        String accessToken = jwtService.createAccessToken(new HashMap<>(), memberDetails.getName());
        String redirectUri = env.getProperty("app.oauth2.authorizedRedirectUris");

        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .build().toUriString();
    }

}
