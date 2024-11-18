package projetvue.springboot_backend.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@trendy.com");
            helper.setTo(to);
            helper.setSubject("Verify Your Account");

            // HTML email body
            String verificationLink = "http://localhost:8080/verify?token=" + verificationToken;
            String htmlContent = """
                <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <div style="max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; padding: 20px; background-color: #f9f9f9;">
                            <h2 style="color: #4CAF50; text-align: center;">Welcome to trendy, explore and have fun!</h2>
                            <p>Thank you for registering with us. To complete your registration, please verify your email by clicking the button below:</p>
                            <div style="text-align: center; margin: 20px 0;">
                                <a href="%s" style="display: inline-block; background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; font-size: 16px; border-radius: 5px;">Verify My Account</a>
                            </div>
                            <p>If the button above doesn't work, copy and paste the following link into your browser:</p>
                            <p style="word-break: break-word;"><a href="%s">%s</a></p>
                            <p>Thank you,<br/>Trendy team</p>
                        </div>
                    </body>
                </html>
            """.formatted(verificationLink, verificationLink, verificationLink);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }
}
