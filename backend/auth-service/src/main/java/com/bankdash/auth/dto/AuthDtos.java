package com.bankdash.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        @NotBlank @Size(min = 3, max = 50) private String username;
        @NotBlank @Size(min = 8) private String password;
        private String phoneNumber;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String identifier;   // email or username
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private UserDto user;

        public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserDto user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.user = user;
        }
    }

    @Data
    public static class UserDto {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String username;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank private String refreshToken;
    }

    @Data
    public static class ValidateTokenRequest {
        @NotBlank private String token;
    }

    @Data
    public static class ValidateTokenResponse {
        private boolean valid;
        private String userId;
        private String username;
        private String email;

        public ValidateTokenResponse(boolean valid) {
            this.valid = valid;
        }
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 8) private String newPassword;
    }
}
