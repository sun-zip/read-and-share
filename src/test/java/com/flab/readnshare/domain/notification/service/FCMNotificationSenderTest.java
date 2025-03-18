package com.flab.readnshare.domain.notification.service;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.notification.domain.LikeNotificationContent;
import com.flab.readnshare.domain.review.domain.Review;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FCMNotificationSenderTest {

    @Mock
    FCMService fcmService;

    @Mock
    FirebaseMessaging firebaseMessaging;

    @InjectMocks
    FCMNotificationSender<LikeNotificationContent> notificationSender;

    @DisplayName("좋아요 이벤트 발행 시 알림이 전송된다.")
    @Test
    void sendLikeNotification() throws Exception {
        // Given
        Member fromMember = ReviewTestFixture.getMemberEntity();
        Review toReview = ReviewTestFixture.getReviewEntity();
        LikeNotificationContent content = LikeNotificationContent.builder()
                .fromMember(fromMember)
                .toReview(toReview)
                .build();

        String fcmToken = "token";
        when(fcmService.getFCMToken(anyLong())).thenReturn(fcmToken);

        ApiFuture<String> futureResponse = ApiFutures.immediateFuture("future");
        when(firebaseMessaging.sendAsync(any(Message.class))).thenReturn(futureResponse);

        // When
        try (MockedStatic<FirebaseMessaging> mockStatic = mockStatic(FirebaseMessaging.class)) {
            mockStatic.when(() -> FirebaseMessaging.getInstance()).thenReturn(firebaseMessaging);
            notificationSender.sendNotification(content);
        }

        // Then
        verify(firebaseMessaging, times(1)).sendAsync(any(Message.class));
    }
}
