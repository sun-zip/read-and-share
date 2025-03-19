package com.flab.readnshare.domain.notification.repository;

import com.flab.readnshare.domain.notification.domain.FCMToken;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FCMTokenRepositoryTest {

    @Autowired
    FCMTokenRepository fcmTokenRepository;

    @AfterEach
    void tearDown() {
        fcmTokenRepository.deleteAll();
    }

    @Nested
    @DisplayName("saveFCMToken 테스트")
    class saveFCMToken {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            FCMToken fcmToken = FCMToken.builder()
                    .memberId(1L)
                    .fcmTokenValue("test")
                    .build();
            fcmTokenRepository.save(fcmToken);

            // When
            Iterable<FCMToken> tokens = fcmTokenRepository.findAll();

            // Then
            Assertions.assertThat(tokens).hasSize(1)
                    .extracting("memberId", "fcmTokenValue")
                    .containsExactly(
                            Tuple.tuple(1L, "test")
                    );
        }
    }
}