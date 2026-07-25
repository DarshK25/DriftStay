package com.driftstay.auth.service;

import com.driftstay.auth.dto.request.DeviceInfo;
import com.driftstay.auth.dto.request.LoginRequest;
import com.driftstay.auth.dto.request.RefreshTokenRequest;
import com.driftstay.auth.dto.request.RegisterRequest;
import com.driftstay.auth.dto.response.AuthResponse;
import com.driftstay.auth.dto.response.UserProfile;
import com.driftstay.common.enums.UserStatus;
import com.driftstay.user.entity.RefreshToken;
import com.driftstay.user.entity.Role;
import com.driftstay.user.entity.User;
import com.driftstay.user.entity.UserPreference;
import com.driftstay.user.repository.RefreshTokenRepository;
import com.driftstay.user.repository.RoleRepository;
import com.driftstay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core authentication service handling user registration, login, token refresh,
 * and logout operations with proper transaction boundaries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Default role assigned to new users
    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";

    /**
     * Register a new user account.
     *
     * @param request Registration details
     * @return AuthResponse with tokens and user profile
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for existing email
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // Create user
        User user = new User();
        user.setPublicId(UUID.randomUUID().toString().replace("-", "").substring(0, 26));
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        user.setIsVerified(false);

        // Assign default role
        Role customerRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(DEFAULT_ROLE);
                    role.setDescription("Default customer role");
                    return roleRepository.save(role);
                });
        user.setRoles(Set.of(customerRole));

        // Create default preferences
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        user.setPreference(preference);

        user = userRepository.save(user);

        log.info("New user registered: {} (email: {})", user.getPublicId(), user.getEmail());

        // Generate tokens
        return generateAuthResponse(user, request.getDeviceInfo());
    }

    /**
     * Authenticate a user and generate tokens.
     *
     * @param request Login credentials
     * @return AuthResponse with tokens and user profile
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCaseAndStatus(
                        request.getEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {} (email: {})", user.getPublicId(), user.getEmail());

        return generateAuthResponse(user, request.getDeviceInfo());
    }

    /**
     * Refresh an access token using a valid refresh token.
     *
     * @param request Refresh token request
     * @return New AuthResponse with fresh tokens
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (storedToken.getRevoked()) {
            // Token was revoked - revoke all tokens for this user (possible token theft)
            refreshTokenRepository.revokeAllUserTokens(storedToken.getUser().getId());
            throw new IllegalArgumentException("Refresh token has been revoked. All sessions invalidated.");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh token has expired. Please login again.");
        }

        // Revoke the used token (token rotation)
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        log.debug("Refresh token rotated for user: {}", storedToken.getUser().getPublicId());

        return generateAuthResponse(storedToken.getUser(), new DeviceInfo());
    }

    /**
     * Logout a user by revoking all their refresh tokens.
     *
     * @param userId The user to logout
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
        log.info("User logged out: {}", userId);
    }

    /**
     * Get the current user's profile.
     *
     * @param userId The user ID
     * @return UserProfile DTO
     */
    @Transactional(readOnly = true)
    public UserProfile getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toUserProfile(user);
    }

    /**
     * Generate an AuthResponse with access and refresh tokens.
     */
    private AuthResponse generateAuthResponse(User user, DeviceInfo deviceInfo) {
        // Generate access token
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roles);

        // Generate and store refresh token
        String rawRefreshToken = jwtService.generateRefreshTokenValue();
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setTokenHash(tokenHash);
        refreshTokenEntity.setExpiresAt(
                LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000));
        refreshTokenEntity.setRevoked(false);

        if (deviceInfo != null) {
            refreshTokenEntity.setDeviceName(deviceInfo.getDeviceName());
            refreshTokenEntity.setDeviceType(deviceInfo.getDeviceType());
        }

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .user(toUserProfile(user))
                .build();
    }

    /**
     * Hash a token using SHA-256 for secure storage.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Map User entity to UserProfile DTO.
     */
    private UserProfile toUserProfile(User user) {
        return UserProfile.builder()
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profilePicture(user.getProfilePicture())
                .isVerified(Boolean.TRUE.equals(user.getIsVerified()))
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
