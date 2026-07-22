package com.driftstay.auth.service;

import com.driftstay.auth.exception.InvalidTokenException;
import com.driftstay.common.enums.UserStatus;
import com.driftstay.user.entity.User;
import com.driftstay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, Long> verificationTokens = new ConcurrentHashMap<>();
    private static final long TTL_HOURS = 24;

    public String generateVerificationToken(Long userId) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        verificationTokens.put(token, userId);
        log.info("Verification token generated for userId={}", userId);
        return token;
    }

    @Transactional
    public void verifyEmail(String token) {
        Long userId = verificationTokens.remove(token);
        if (userId == null) {
            throw new InvalidTokenException("Invalid or expired verification token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found after verification"));

        user.setIsVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Email verified for userId={}", userId);
    }

    public boolean isVerified(Long userId) {
        return userRepository.findById(userId)
                .map(User::getIsVerified)
                .orElse(false);
    }
}
