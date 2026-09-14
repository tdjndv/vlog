package com.vlog.vlog.auth;

import com.vlog.vlog.auth.dto.AuthResponse;
import com.vlog.vlog.auth.dto.LoginRequest;
import com.vlog.vlog.user.User;
import com.vlog.vlog.user.UserService;
import com.vlog.vlog.user.dto.CreateUserRequest;
import com.vlog.vlog.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody CreateUserRequest request) {
        // role is ignored here — everyone signs up as USER
        UserResponse created = userService.createStandardUser(request);

        String token = jwtService.issue(created.username(), created.role().name());
        return AuthResponse.of(token, jwtService.expiresInSeconds(),
                created.username(), created.role());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userService.getEntityByUsername(request.username());
        String token = jwtService.issue(user.getUsername(), user.getRole().name());

        return AuthResponse.of(token, jwtService.expiresInSeconds(),
                user.getUsername(), user.getRole());
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }
}