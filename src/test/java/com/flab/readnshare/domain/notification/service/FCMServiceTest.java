package com.flab.readnshare.domain.notification.service;

import com.flab.readnshare.global.common.exception.MemberException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FCMServiceTest {

    @Autowired
    FCMService fcmService;

    @DisplayName("존재하지 않는 유저의 FCM 토큰을 조회할 시 MEMBER_NOT_FOUND 에러가 발생한다.")
    @Test
    void getFcmToken_fail_not_found() throws Exception {
        // Given
        Long memberId = 9L;
        // When
        // Then
        Assertions.assertThatThrownBy(() -> fcmService.getFCMToken(memberId))
                .isInstanceOf(MemberException.MemberNotFoundException.class)
                .hasMessage("존재하지 않는 회원입니다.");

    }

}