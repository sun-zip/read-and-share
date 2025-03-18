package com.flab.readnshare.domain.review.event;

import com.flab.readnshare.domain.feed.facade.FeedFacade;
import com.flab.readnshare.domain.follow.service.FollowService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewEventListener {
    private final FollowService followService;
    private final FeedFacade feedFacade;

    @TransactionalEventListener
    private void updateFollowersFeed(ReviewCreateEvent event) {
        Member writer = event.getMember();
        Review review = event.getReview();

        List<Long> followerIds = followService.getFollowerIds(writer);

        if (!followerIds.isEmpty()) {
            feedFacade.addToFeed(followerIds, review);
        }
    }

    @TransactionalEventListener
    private void deleteReviewFromFeed(ReviewDeleteEvent event) {
        Member writer = event.getMember();
        Long reviewId = event.getReviewId();

        List<Long> followerIds = followService.getFollowerIds(writer);
        if (!followerIds.isEmpty()) {
            feedFacade.deleteToFeed(followerIds, reviewId);
        }
    }
}
