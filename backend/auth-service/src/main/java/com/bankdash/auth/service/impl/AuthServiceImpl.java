package com.bankdash.auth.service.impl;

import com.bankdash.auth.dto.AuthDtos.*;
import com.bankdash.auth.entity.User;
import com.bankdash.auth.entity.UserStatus;
import com.bankdash.auth.exception.AuthException;
import com.bankdash.auth.repository.UserRepository;
import com.bankdash.auth.security.JwtTokenProvider;
import com.bankdash.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklisted_token:";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());
        return buildAuthResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AuthException("Invalid credentials");
        }

        User user = userRepository.findByEmailOrUsername(request.getIdentifier())
                .orElseThrow(() -> new AuthException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthException("Account is " + user.getStatus().name().toLowerCase());
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.isTokenValid(token)) {
            throw new AuthException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken  = generateTokenWithUserId(user, userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(newAccessToken, newRefreshToken, jwtTokenProvider.getExpirationMs(), toUserDto(user));
    }

    @Override
    public ValidateTokenResponse validateToken(String token) {
        Boolean isBlacklisted = redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token);
        if (Boolean.TRUE.equals(isBlacklisted)) return new ValidateTokenResponse(false);

        if (!jwtTokenProvider.isTokenValid(token)) return new ValidateTokenResponse(false);

        String username = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return new ValidateTokenResponse(false);

        ValidateTokenResponse response = new ValidateTokenResponse(true);
        response.setUserId(user.getId().toString());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }

    @Override
    public void logout(String token) {
        if (jwtTokenProvider.isTokenValid(token)) {
            long ttl = jwtTokenProvider.extractExpiration(token).getTime() - System.currentTimeMillis();
            redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "blacklisted", ttl, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken  = generateTokenWithUserId(user, userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        return new AuthResponse(accessToken, refreshToken, jwtTokenProvider.getExpirationMs(), toUserDto(user));
    }

    // Store the real UUID in the JWT so the gateway can forward it correctly
    private String generateTokenWithUserId(User user, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email",  user.getEmail());
        return jwtTokenProvider.generateAccessToken(claims, userDetails);
    }

    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId().toString());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        return dto;
    }
}
