package com.smartinventorysystem.common.email;

public interface EmailService {

    void sendStaffAccountCreatedEmail(String toEmail, String fullName, String token);

    void sendResetPasswordEmail(String toEmail, String fullName, String token);
}

