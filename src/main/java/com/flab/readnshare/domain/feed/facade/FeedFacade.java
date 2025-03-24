package com.flab.readnshare.domain.feed.facade;

import com.flab.readnshare.domain.feed.dto.FeedResponseDto;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FeedFacade {
    private final RedisTemplate<String, Object> feedRedisTemplate;
    private final ReviewService reviewService;

    private static final String KEY = "user:%d:feed";
    private static final long FEED_EXPIRE_DURATION = 7;

    public void addToFeed(List<Long> followerIds, Review review) {
        Long timestamp = System.currentTimeMillis();
        Long reviewId = review.getId();

        feedRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            followerIds.stream().map(followerId -> getUserFeedKey(followerId))
                    .forEach(userFeedKey -> {
                        feedRedisTemplate.boundZSetOps(userFeedKey).add(String.valueOf(reviewId), timestamp);
                        feedRedisTemplate.boundZSetOps(userFeedKey).expire(FEED_EXPIRE_DURATION, TimeUnit.DAYS);
                    });
            return null;
        });
    }

    public List<FeedResponseDto> getFeeds(Long memberId, Long lastReviewId, int limit) {
        String userFeedKey = getUserFeedKey(memberId);

        Set<Object> feedSet = getFeedSet(userFeedKey, lastReviewId, limit);

        List<Long> reviewIds = getReviewIdsFrom(feedSet);
        List<Review> reviews = reviewService.findByIdIn(reviewIds);

        return reviews.stream()
                .map(review -> FeedResponseDto.from(review))
                .toList();
    }

    public void deleteToFeed(List<Long> followerIds, Long reviewId) {
        feedRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            followerIds.stream()
                    .map(this::getUserFeedKey)
                    .forEach(userFeedKey -> feedRedisTemplate.opsForZSet().remove(userFeedKey, String.valueOf(reviewId)));
            return null;
        });
    }

    private String getUserFeedKey(Long followerId) {
        return String.format(KEY, followerId);
    }

    private Set<Object> getFeedSet(String userFeedKey, Long lastReviewId, int limit) {
        if (lastReviewId == null) {
            return feedRedisTemplate.opsForZSet().reverseRange(userFeedKey, 0, (limit - 1));
        }

        Double score = feedRedisTemplate.opsForZSet().score(userFeedKey, String.valueOf(lastReviewId));
        if (score == null) {
            return Collections.emptySet();
        }

        return feedRedisTemplate.opsForZSet().reverseRangeByScore(userFeedKey, Double.MIN_VALUE, (score - 1), 0, limit);
    }

    private List<Long> getReviewIdsFrom(Set<Object> feedSet) {
        return Optional.ofNullable(feedSet)
                .orElse(Collections.emptySet())
                .stream()
                .map(reviewId -> Long.parseLong((String) reviewId))
                .toList();
    }

}