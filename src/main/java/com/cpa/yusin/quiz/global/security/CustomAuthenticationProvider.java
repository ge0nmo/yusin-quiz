package com.cpa.yusin.quiz.global.security;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
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
            throw new MemberException(ExceptionMessage.USER_NOT_FOUND);

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
        validatePassword(password, memberDetails);
        validatePlatform(memberDetails);
    }

    private void validatePassword(String password, MemberDetails memberDetails)
    {
        if(!passwordEncoder.matches(password, memberDetails.getPassword())){
            throw new MemberException(ExceptionMessage.USER_NOT_FOUND);
        }
    }

    private void validatePlatform(MemberDetails memberDetails)
    {
        if(!Platform.HOME.equals(memberDetails.getMember().getPlatform())) {
            throw new MemberException(ExceptionMessage.USER_NOT_FOUND);
        }
    }
}
