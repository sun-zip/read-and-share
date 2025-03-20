package com.flab.readnshare.domain.notification.event;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.follow.event.FollowEvent;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.notification.domain.FollowNotificationContent;
import com.flab.readnshare.domain.notification.service.NotificationSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FollowEventListenerTest {

    @Mock
    NotificationSender<FollowNotificationContent> notificationSender;

    @InjectMocks
    FollowEventListener followEventListener;

    @Nested
    @DisplayName("handle 테스트")
    class handleTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member toMember = ReviewTestFixture.getMemberEntity();
            Member fromMember = ReviewTestFixture.getMemberEntity();
            FollowEvent event = new FollowEvent(new Object(), fromMember, toMember);

            willDoNothing()
                    .given(notificationSender).sendNotification(any(FollowNotificationContent.class));

            // When
            followEventListener.handle(event);

            // Then
            verify(notificationSender, times(1)).sendNotification(any(FollowNotificationContent.class));
        }
    }

}