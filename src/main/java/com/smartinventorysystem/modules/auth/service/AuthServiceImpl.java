package com.smartinventorysystem.modules.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.smartinventorysystem.common.email.EmailService;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.enums.Role;
import com.smartinventorysystem.enums.Status;
import com.smartinventorysystem.exceptions.BadRequestException;
import com.smartinventorysystem.exceptions.EmailAlreadyExistedException;
import com.smartinventorysystem.exceptions.UnauthorizedException;
import com.smartinventorysystem.modules.auth.dto.request.*;
import com.smartinventorysystem.modules.auth.dto.response.AuthResponse;
import com.smartinventorysystem.modules.auth.mapper.AuthUserMapper;
import com.smartinventorysystem.modules.user.repository.UserRepository;
import com.smartinventorysystem.modules.user.entity.User;
import com.smartinventorysystem.security.JwtUtil;
import com.smartinventorysystem.security.TokenBlacklist;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final AuthUserMapper authUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;
    private final Clock clock;
    private final EmailService emailService;


    @Override
    public AuthResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistedException("Email already exists");
        }

        User user = authUserMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);

        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now(clock));

        return authUserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(MessageConstants.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid password");
        }

        if (user.getStatus() != Status.ACTIVE) {
            throw new DisabledException("Your account has been deactivated.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserID());

        AuthResponse response = authUserMapper.toResponse(user);
        response.setStatus(user.getStatus().name());
        response.setToken(token);
        return response;
    }

    public void logout(String token) {
        tokenBlacklist.add(token);
    }

    @Override
    public void activateAccount(ActivateAccountRequest request) {

        User user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid activation token"));

        // check expiry
        if (user.getTokenExpiry().isBefore(LocalDateTime.now(clock))) {
            throw new BadRequestException("Activation link expired");
        }

        // set password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // activate account
        user.setStatus(Status.ACTIVE);

        // clear token
        user.setActivationToken(null);
        user.setTokenExpiry(null);

        // Update
        user.setUpdatedAt(LocalDateTime.now(clock));
        userRepository.save(user);
    }

    @Override
    public void resendActivationLink(ResendActivationRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException(MessageConstants.USER_NOT_FOUND));

        if (user.getStatus() == Status.ACTIVE) {
            throw new BadRequestException("Account is already activated");
        }

        String token = UUID.randomUUID().toString();

        user.setActivationToken(token);
        user.setTokenExpiry(LocalDateTime.now(clock).plusHours(24));
        user.setUpdatedAt(LocalDateTime.now(clock));

        userRepository.save(user);

        emailService.sendStaffAccountCreatedEmail(
                user.getEmail(),
                user.getFullName(),
                token);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found with email: " + request.getEmail()));
        String token = java.util.UUID.randomUUID().toString();
        // Reusing activationToken & tokenExpiry as requested
        user.setActivationToken(token);
        user.setTokenExpiry(LocalDateTime.now(clock).plusHours(24));
        user.setUpdatedAt(LocalDateTime.now(clock));
        userRepository.save(user);
        emailService.sendResetPasswordEmail(
                user.getEmail(),
                user.getFullName(),
                token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));
        if (user.getTokenExpiry().isBefore(LocalDateTime.now(clock))) {
            throw new BadRequestException("Password reset link expired");
        }
        // Set password and activate account if not already active (just in case)
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(com.smartinventorysystem.enums.Status.ACTIVE);
        // Clear token
        user.setActivationToken(null);
        user.setTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now(clock));
        userRepository.save(user);
    }

    @Override
    public void verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Token is required");
        }
        User user = userRepository.findByActivationToken(token.trim())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));
        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now(clock))) {
            throw new BadRequestException("Token has expired");
        }
    }

    @Override
    public AuthResponse authenticateGoogleUser(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                User existingUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UnauthorizedException(
                                "User with this email does not exist. Please register via manual sign-up first."));

                if (existingUser.getStatus() != Status.ACTIVE) {
                    throw new DisabledException("Your account has been deactivated.");
                }

                String token = jwtUtil.generateToken(existingUser.getEmail(), existingUser.getRole().name(),
                        existingUser.getUserID());

                AuthResponse response = authUserMapper.toResponse(existingUser);
                response.setStatus(existingUser.getStatus().name());
                response.setToken(token);
                return response;
            } else {
                throw new UnauthorizedException("Invalid Google ID token.");
            }
        } catch (UnauthorizedException | DisabledException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Google authentication failed: " + e.getMessage());
        }
    }

}
