package com.flab.readnshare.domain.notification.service;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.domain.notification.repository.FCMTokenRepository;
import com.flab.readnshare.global.common.exception.MemberException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FCMServiceTest {

    @Autowired
    FCMService fcmService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FCMTokenRepository fcmTokenRepository;

    @AfterEach
    void tearDown() {
        fcmTokenRepository.deleteAll();
    }

    @Nested
    @DisplayName("getFCMToken 테스트")
    class getFCMTokenTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member member = createMember("testNick");
            memberRepository.save(member);
            String token = "token";
            fcmService.saveFCMToken(member, token);

            // When
            String fcmToken = fcmService.getFCMToken(member.getId());

            // Then
            assertThat(fcmToken).isEqualTo("token");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저의 FCM 토큰 조회")
        void fail_not_found_user() throws Exception {
            // Given
            Long memberId = 9L;
            // When
            // Then
            assertThatThrownBy(() -> fcmService.getFCMToken(memberId))
                    .isInstanceOf(MemberException.MemberNotFoundException.class)
                    .hasMessage("존재하지 않는 회원입니다.");

        }
    }

    private Member createMember(String nickName) {
        return Member.builder()
                .email("test")
                .password(null)
                .nickName(nickName)
                .build();
    }
}