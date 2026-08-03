package com.smartinventorysystem.modules.auth.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.modules.auth.dto.request.ActivateAccountRequest;
import com.smartinventorysystem.modules.auth.dto.request.ResendActivationRequest;
import com.smartinventorysystem.modules.auth.dto.response.AuthResponse;
import com.smartinventorysystem.modules.auth.dto.request.LoginRequest;
import com.smartinventorysystem.modules.auth.dto.request.SignupRequest;
import com.smartinventorysystem.modules.auth.dto.request.ForgotPasswordRequest;
import com.smartinventorysystem.modules.auth.dto.request.ResetPasswordRequest;
import com.smartinventorysystem.modules.auth.dto.request.GoogleLoginRequest;
import com.smartinventorysystem.modules.auth.service.AuthService;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiRoutes.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final Clock clock;

    @PostMapping(ApiRoutes.Auth.SIGNUP)
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Signup successful")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Login successful")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.LOGIN_GOOGLE)
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.authenticateGoogleUser(request.getIdToken());
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Google Login successful")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.LOGOUT)
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(jwtUtil.extractToken(authHeader));
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Logged out successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.ACTIVATE)
    public ResponseEntity<ApiResponse<Void>> activateAccount(
            @Valid @RequestBody ActivateAccountRequest request) {

        authService.activateAccount(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Staff Account Activated Successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.RESEND_ACTIVATE)
    public ResponseEntity<ApiResponse<Void>> resendActivationLink(
            @Valid @RequestBody ResendActivationRequest request) {

        authService.resendActivationLink(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("New activation link has been sent.")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.FORGOT_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Password reset link has been sent to your email.")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.RESET_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Password has been reset successfully.")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

}
