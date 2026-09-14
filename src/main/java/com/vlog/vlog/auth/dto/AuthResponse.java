package com.vlog.vlog.auth.dto;

import com.vlog.vlog.user.Role;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        String username,
        Role role
) {
    public static AuthResponse of(String token, long expiresIn, String username, Role role) {
        return new AuthResponse(token, "Bearer", expiresIn, username, role);
    }
}