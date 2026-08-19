package com.smartinventorysystem.common.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void sendStaffAccountCreatedEmail(String toEmail, String fullName, String token) {
        String activationLink = frontendUrl + "/activate?token=" + token;
        String subject = "Activate Your Smart Inventory Account";

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, Helvetica, sans-serif; background-color:#f4f4f4; padding:30px;">
                    <table style="max-width:600px; margin:auto; background:#ffffff; border-radius:8px; padding:40px;">
                        <tr>
                            <td>
                                <h2 style="color:#2563eb; margin-bottom:20px;">
                                    Smart Inventory System
                                </h2>
                                <p>Hello <strong>%s</strong>,</p>
                                <p>
                                    Your staff account has been created successfully.
                                </p>
                                <p>
                                    To activate your account and set your password, please click the button below:
                                </p>
                                <p style="text-align:center; margin:35px 0;">
                                    <a href="%s"
                                       style="
                                            background-color:#2563eb;
                                            color:#ffffff;
                                            text-decoration:none;
                                            padding:14px 28px;
                                            border-radius:6px;
                                            display:inline-block;
                                            font-weight:bold;">
                                        Activate Account
                                    </a>
                                </p>
                                <p>
                                    This link will expire in <strong>24 hours</strong>.
                                </p>
                                <hr>
                                <p style="font-size:13px; color:#666666;">
                                    If you did not expect this email, please contact your administrator.
                                </p>
                                <p style="font-size:13px; color:#666666;">
                                    Regards,<br>
                                    <strong>Smart Inventory System Team</strong>
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(fullName, activationLink);

        sendHtmlEmail(toEmail, subject, html, "Failed to send staff account activation email.");
    }

    @Override
    public void sendResetPasswordEmail(String toEmail, String fullName, String token) {
        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
        String subject = "Reset Your Smart Inventory Password";

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, Helvetica, sans-serif; background-color:#f4f4f4; padding:30px;">
                    <table style="max-width:600px; margin:auto; background:#ffffff; border-radius:8px; padding:40px;">
                        <tr>
                            <td>
                                <h2 style="color:#2563eb; margin-bottom:20px;">
                                    Smart Inventory System
                                </h2>
                                <p>Hello <strong>%s</strong>,</p>
                                <p>
                                    We received a request to reset the password for your Smart Inventory account.
                                </p>
                                <p>
                                    Click the button below to choose a new password.
                                </p>
                                <p style="text-align:center; margin:35px 0;">
                                    <a href="%s"
                                       style="
                                            background-color:#2563eb;
                                            color:#ffffff;
                                            text-decoration:none;
                                            padding:14px 28px;
                                            border-radius:6px;
                                            display:inline-block;
                                            font-weight:bold;">
                                        Reset Password
                                    </a>
                                </p>
                                <p>
                                    This password reset link will expire in
                                    <strong>24 hours</strong>.
                                </p>
                                <hr>
                                <p style="font-size:13px; color:#666666;">
                                    If you did not make this request, you can safely ignore this email. Your password will remain unchanged.
                                </p>
                                <p style="font-size:13px; color:#666666;">
                                    Regards,<br>
                                    <strong>Smart Inventory System Team</strong>
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(fullName, resetLink);

        sendHtmlEmail(toEmail, subject, html, "Failed to send password reset email.");
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent, String errorMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("{}: {}", errorMessage, e.getMessage(), e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
