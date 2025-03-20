package com.flab.readnshare.domain.notification.domain;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LikeNotificationContent implements NotificationContent {
    private Member fromMember;
    private Review toReview;
    private Member toMember;

    @Builder
    public LikeNotificationContent(Member fromMember, Review toReview) {
        this.fromMember = fromMember;
        this.toReview = toReview;
        this.toMember = toReview.getMember();
    }

    @Override
    public String getTitle() {
        return "좋아요";
    }

    @Override
    public String getBody() {
        return fromMember.getNickName() + "님이 " + toReview.getContent() + " 리뷰에 좋아요를 눌렀습니다.";
    }

    @Override
    public Long getReceiverId() {
        return toMember.getId();
    }

}
