package com.smartinventorysystem.common.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetPasswordEmail implements ResetPasswordEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendResetPasswordEmail(String toEmail, String fullName, String token) {

        String resetLink = "http://localhost:4200/auth/reset-password?token=" + token;

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
                """
                .formatted(fullName, resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email.", e);
        }
    }
}