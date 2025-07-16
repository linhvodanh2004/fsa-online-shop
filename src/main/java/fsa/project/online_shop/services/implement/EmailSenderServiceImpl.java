package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.services.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {
    private final JavaMailSender javaMailSender;

    @Override
    public void sendVerificationCode(String email, String username, String code) throws MessagingException {
        String subject = "🔐 Reset Your Password - Verification Code";
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2 style='color: #333;'>Forgot your password?</h2>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>We received a request to reset your password. Use the verification code below to proceed:</p>"
                + "<div style='background-color: #f3f4f6; padding: 10px; text-align: center; font-size: 24px; "
                + "letter-spacing: 2px; font-weight: bold; color: #1d4ed8; border-radius: 5px;'>"
                + code + "</div>"
                + "<p>This code will expire in 5 minutes. If you didn’t request a password reset, please ignore this email.</p>"
                + "<hr style='margin-top: 30px;'>"
                + "<p style='font-size: 12px; color: gray;'>Gaming Online Shop Team</p>"
                + "</div>";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("linhvodanh2004@gmail.com");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true); // true = isHtml
        javaMailSender.send(message);
    }
}
