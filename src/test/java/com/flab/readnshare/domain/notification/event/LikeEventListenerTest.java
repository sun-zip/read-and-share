package com.flab.readnshare.domain.notification.event;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.notification.domain.LikeNotificationContent;
import com.flab.readnshare.domain.notification.service.NotificationSender;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.event.LikeEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LikeEventListenerTest {

    @Mock
    NotificationSender<LikeNotificationContent> notificationSender;

    @InjectMocks
    LikeEventListener likeEventListener;

    @Nested
    @DisplayName("handle 테스트")
    class handleTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Review review = ReviewTestFixture.getReviewEntity();
            Member member = ReviewTestFixture.getMemberEntity();
            LikeEvent event = new LikeEvent(new Object(), member, review);

            willDoNothing()
                    .given(notificationSender).sendNotification(any(LikeNotificationContent.class));

            // When
            likeEventListener.handle(event);

            // Then
            verify(notificationSender, times(1)).sendNotification(any(LikeNotificationContent.class));
        }
    }
}