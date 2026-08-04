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
public class SendStaffAccountCreatedEmail implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendStaffAccountCreatedEmail(String toEmail,
                                             String fullName,
                                             String token) {

        String activationLink = "http://localhost:4200/activate?token=" + token;

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
                                    <strong>Login Email:</strong><br>
                                    %s
                                </p>

                                <p>
                                    Click the button below to activate your account and set your password.
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
                                    This activation link will expire in
                                    <strong>24 hours</strong>.
                                </p>

                                <hr>

                                <p style="font-size:13px; color:#666666;">
                                    If you did not expect this email, you can safely ignore it.
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
                """.formatted(fullName, toEmail, activationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send activation email.", e);
        }
    }
}