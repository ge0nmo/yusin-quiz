package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.WordPracticeException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Component
public class GuestTokenHasher {

    public String hash(String rawToken) {
        validate(rawToken);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return toLowercaseHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    private void validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidGuestToken();
        }
        try {
            UUID parsedToken = UUID.fromString(rawToken);
            if (!parsedToken.toString().equalsIgnoreCase(rawToken)) {
                throw invalidGuestToken();
            }
        } catch (IllegalArgumentException exception) {
            throw invalidGuestToken();
        }
    }

    private String toLowercaseHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(Character.forDigit((value >>> 4) & 0xF, 16));
            result.append(Character.forDigit(value & 0xF, 16));
        }
        return result.toString();
    }

    private WordPracticeException invalidGuestToken() {
        return new WordPracticeException(ExceptionMessage.WORD_PRACTICE_INVALID_GUEST_TOKEN);
    }
}
