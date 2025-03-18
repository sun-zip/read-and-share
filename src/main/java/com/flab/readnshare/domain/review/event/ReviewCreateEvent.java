package com.flab.readnshare.domain.review.event;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import lombok.Getter;

@Getter
public class ReviewCreateEvent {
    private final Member member;
    private final Review review;

    public ReviewCreateEvent(Member member, Review review) {
        this.member = member;
        this.review = review;
    }
}
