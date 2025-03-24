package com.flab.readnshare.domain.member.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // @Value 어노테이션으로 주입되는 fromEmail 값을 직접 설정
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
    }

    @Nested
    @DisplayName("sendVerificationEmail 테스트")
    class sendVerificationEmail {
        @Test
        @DisplayName("성공")
        void success() throws MessagingException {
            // given
            String toEmail = "test@naver.com";
            String token = "testToken123";

            // MimeMessage 모킹
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // when
            emailService.sendVerificationEmail(toEmail, token);

            // then
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("실패 - 이메일 전송 실패")
        void fail_sendFail() throws MessagingException {
            // given
            String toEmail = "test@naver.com";
            String token = "testToken123";

            // MimeMessage 객체 모킹
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // 예외 던지기 설정 - MailSendException 사용
            doThrow(new MailSendException("이메일 전송에 실패했습니다.")).when(mailSender).send(any(MimeMessage.class));

            // when & then
            MessagingException thrown = assertThrows(MessagingException.class, () -> {
                emailService.sendVerificationEmail(toEmail, token);
            });

            assertEquals("이메일 전송에 실패했습니다.", thrown.getMessage());
        }
    }
}