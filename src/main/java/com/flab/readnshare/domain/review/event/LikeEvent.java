package com.flab.readnshare.domain.review.event;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LikeEvent extends ApplicationEvent {

    private final Member fromMember;
    private final Review toReview;

    public LikeEvent(Object source, Member fromMember, Review toReview) {
        super(source);
        this.fromMember = fromMember;
        this.toReview = toReview;
    }
}
