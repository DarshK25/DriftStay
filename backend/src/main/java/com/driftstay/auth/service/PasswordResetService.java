package com.driftstay.auth.service;

import com.driftstay.auth.exception.InvalidTokenException;
import com.driftstay.common.exception.ResourceNotFoundException;
import com.driftstay.common.enums.UserStatus;
import com.driftstay.user.entity.User;
import com.driftstay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final ConcurrentHashMap<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

    private static final long TOKEN_TTL_MINUTES = 60;

    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmailIgnoreCaseAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        resetTokens.put(tokenHash, new ResetToken(user.getId(), LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES)));

        log.info("Password reset requested for user={} tokenHash={}", user.getEmail(), tokenHash);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);
        ResetToken resetToken = Optional.ofNullable(resetTokens.get(tokenHash))
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            resetTokens.remove(tokenHash);
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = userRepository.findById(resetToken.userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", resetToken.userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(tokenHash);
        log.info("Password reset completed for userId={}", user.getId());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private record ResetToken(Long userId, LocalDateTime expiresAt) {}
}
