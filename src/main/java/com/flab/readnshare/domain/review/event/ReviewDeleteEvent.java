package com.flab.readnshare.domain.review.event;

import com.flab.readnshare.domain.member.domain.Member;
import lombok.Getter;

@Getter
public class ReviewDeleteEvent {
    private final Member member;
    private final Long reviewId;

    public ReviewDeleteEvent(Member member, Long reviewId) {
        this.member = member;
        this.reviewId = reviewId;
    }
}
