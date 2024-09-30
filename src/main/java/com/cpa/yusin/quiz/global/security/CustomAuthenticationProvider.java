package com.cpa.yusin.quiz.global.security;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider
{
    private final MemberDetailsService memberDetailsService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        String email = authentication.getName();

        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(email);

        if(memberDetails == null)
            throw new GlobalException(ExceptionMessage.USER_NOT_FOUND);

        String password = authentication.getCredentials().toString();

        validateMember(password, memberDetails);

        return new UsernamePasswordAuthenticationToken(email, null, memberDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void validateMember(String password, MemberDetails memberDetails)
    {
        if(!validatePassword(password, memberDetails) || !validatePlatform(memberDetails))
            throw new GlobalException(ExceptionMessage.USER_NOT_FOUND);
    }

    private boolean validatePassword(String password, MemberDetails memberDetails)
    {
        return passwordEncoder.matches(password, memberDetails.getPassword());
    }

    private boolean validatePlatform(MemberDetails memberDetails)
    {
        return Platform.HOME.equals(memberDetails.getMember().getPlatform());
    }
}
