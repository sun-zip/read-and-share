package com.flab.readnshare.domain.notification.event;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.notification.domain.LikeNotificationContent;
import com.flab.readnshare.domain.notification.service.NotificationSender;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LikeEventListener {
    private final NotificationSender<LikeNotificationContent> notificationSender;

    @TransactionalEventListener
    public void handle(LikeEvent event) {
        Member fromMember = event.getFromMember();
        Review toReview = event.getToReview();

        LikeNotificationContent content = LikeNotificationContent
                .builder()
                .fromMember(fromMember)
                .toReview(toReview)
                .build();

        notificationSender.sendNotification(content);
    }
}
