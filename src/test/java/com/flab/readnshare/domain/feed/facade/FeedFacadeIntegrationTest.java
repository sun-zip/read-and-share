package com.flab.readnshare.domain.feed.facade;

import com.flab.readnshare.FeedTestFixture;
import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FeedFacadeIntegrationTest {

    @Autowired
    FeedFacade feedFacade;

    @Autowired
    ReviewService reviewService;

    @Autowired
    RedisTemplate<String, Object> feedRedisTemplate;

    @DisplayName("특정 피드를 삭제한다.")
    @Test
    void deleteFeed() throws Exception {
        // Given
        List<Long> followerIds = FeedTestFixture.getFollowerTestIds();
        Review review = ReviewTestFixture.getReviewEntity();
        Long reviewId = review.getId();
        Long timestamp = System.currentTimeMillis();

        for (Long followerId : followerIds) {
            String userFeedKey = String.format("user:%d:feed", followerId);
            feedRedisTemplate.opsForZSet().add(userFeedKey, String.valueOf(reviewId), timestamp);
        }

        // When
        feedFacade.deleteToFeed(followerIds, reviewId);

        // Then
        for (Long followerId : followerIds) {
            String userFeedKey = String.format("user:%d:feed", followerId);
            Set<Object> beforeDelete = feedRedisTemplate.opsForZSet().range(userFeedKey, 0, -1);
            assertThat(beforeDelete).hasSize(0)
                    .isEmpty();
        }
    }
}
