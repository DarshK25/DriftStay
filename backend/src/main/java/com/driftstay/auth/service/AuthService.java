package com.driftstay.auth.service;

import com.driftstay.auth.dto.request.LoginRequest;
import com.driftstay.auth.dto.request.RefreshTokenRequest;
import com.driftstay.auth.dto.request.RegisterRequest;
import com.driftstay.auth.dto.response.AuthResponse;
import com.driftstay.auth.entity.UserPrincipal;
import com.driftstay.auth.exception.EmailAlreadyExistsException;
import com.driftstay.auth.exception.InvalidTokenException;
import com.driftstay.auth.repository.RefreshTokenRepository;
import com.driftstay.config.JwtProperties;
import com.driftstay.common.enums.UserStatus;
import com.driftstay.common.exception.ResourceNotFoundException;
import com.driftstay.user.entity.RefreshToken;
import com.driftstay.user.entity.Role;
import com.driftstay.user.entity.User;
import com.driftstay.user.repository.RoleRepository;
import com.driftstay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailVerificationService emailVerificationService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setStatus(UserStatus.ACTIVE);
        user.setIsVerified(false);
        user.setRoles(new HashSet<>(Set.of(userRole)));

        user = userRepository.save(user);

        String verificationToken = emailVerificationService.generateVerificationToken(user.getId());
        log.info("Verification token for userId={}: {}", user.getId(), verificationToken);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefreshToken = createRefreshToken(user, null, null);

        return buildAuthResponse(accessToken, rawRefreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmailIgnoreCaseAndStatus(
                request.getEmail().toLowerCase().trim(), UserStatus.ACTIVE)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefreshToken = createRefreshToken(user, null, null);

        return buildAuthResponse(accessToken, rawRefreshToken);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = jwtService.hashToken(request.getRefreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (storedToken.getRevoked() || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefreshToken = createRefreshToken(user, storedToken.getDeviceName(), storedToken.getDeviceType());

        return buildAuthResponse(accessToken, rawRefreshToken);
    }

    public void logout(RefreshTokenRequest request) {
        String tokenHash = jwtService.hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("User {} logged out, refresh token revoked", token.getUser().getId());
        });
    }

    public void logoutAll(String userEmail) {
        User user = userRepository.findByEmailIgnoreCaseAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        refreshTokenRepository.deleteByUserId(user.getId());
        log.info("All sessions revoked for userId={}", user.getId());
    }

    public long getActiveSessions(String userEmail) {
        User user = userRepository.findByEmailIgnoreCaseAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        return refreshTokenRepository.countByUserId(user.getId());
    }

    private String createRefreshToken(User user, String deviceName, String deviceType) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = jwtService.hashToken(rawToken);

        long expirationMs = Long.parseLong(jwtProperties.getRefreshTokenExpiration());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000));
        refreshToken.setRevoked(false);
        refreshToken.setDeviceName(deviceName);
        refreshToken.setDeviceType(deviceType);

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private AuthResponse buildAuthResponse(String accessToken, String rawRefreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(Long.parseLong(jwtProperties.getAccessTokenExpiration()))
                .build();
    }

    public User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
