package com.smartinventorysystem.modules.auth.service;

import com.smartinventorysystem.modules.auth.dto.request.*;
import com.smartinventorysystem.modules.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse  signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);

    void activateAccount(ActivateAccountRequest request);

    void resendActivationLink(ResendActivationRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    AuthResponse authenticateGoogleUser(String idTokenString);

}
