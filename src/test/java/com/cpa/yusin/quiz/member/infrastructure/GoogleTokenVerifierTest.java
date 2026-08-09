package com.cpa.yusin.quiz.member.infrastructure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GoogleTokenVerifierTest {

    @Test
    void parserFailureDoesNotLogExternalTokenOrParserMessage() throws Exception {
        String externalToken = "external-token-must-not-be-logged";
        GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
        given(verifier.verify(externalToken))
                .willThrow(new IOException("parser detail includes " + externalToken));
        GoogleTokenVerifier googleTokenVerifier = new GoogleTokenVerifier(verifier);

        Logger logger = (Logger) LoggerFactory.getLogger(GoogleTokenVerifier.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThatThrownBy(() -> googleTokenVerifier.verify(externalToken))
                    .isInstanceOf(MemberException.class)
                    .satisfies(exception -> assertThat(((MemberException) exception).getExceptionMessage())
                            .isEqualTo(ExceptionMessage.INVALID_SOCIAL_TOKEN));

            assertThat(appender.list)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .allSatisfy(message -> {
                        assertThat(message).doesNotContain(externalToken);
                        assertThat(message).doesNotContain("parser detail");
                    });
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
