package com.bankdash.auth.service;

import com.bankdash.auth.dto.AuthDtos.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    ValidateTokenResponse validateToken(String token);
    void logout(String token);
    void changePassword(String username, ChangePasswordRequest request);
}
