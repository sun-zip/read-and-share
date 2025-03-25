package com.flab.readnshare.domain.member.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") // 발신자 이메일 (application.yml 설정 필요)
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) throws MessagingException {
        String subject = "ReadNShare 이메일 인증을 완료해주세요!";
        String verificationLink = "http://localhost:8080/api/v1/members/verify?token=" + token;

        // HTML 이메일 내용
        String content = "<html>"
                + "<body>"
                + "<h2>이메일 인증을 완료하세요</h2>"
                + "<p>다음 버튼을 클릭하여 이메일 인증을 완료하세요:</p>"
                + "<a href='" + verificationLink + "' style='"
                + "display: inline-block;"
                + "padding: 10px 20px;"
                + "background-color: #4CAF50;"
                + "color: white;"
                + "text-align: center;"
                + "text-decoration: none;"
                + "border-radius: 5px;'>이메일 인증</a>"
                + "</body>"
                + "</html>";

        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");  // 인코딩을 UTF-8로 설정

        messageHelper.setFrom(fromEmail);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(content, true);  // true를 설정하면 HTML 이메일로 보냄

        try {
            mailSender.send(mimeMessage);
        } catch (MailException e) {
            e.printStackTrace();
            throw new MessagingException("이메일 전송에 실패했습니다.", e);
        }
    }
}
