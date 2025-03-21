package com.flab.readnshare.domain.feed.facade;

import com.flab.readnshare.FeedTestFixture;
import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.feed.dto.FeedResponseDto;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedFacadeTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ReviewService reviewService;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @InjectMocks
    private FeedFacade feedFacade;

    @Nested
    @DisplayName("addToFeed 테스트")
    class addToFeedTest {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            List<Long> followerIds = FeedTestFixture.getFollowerTestIds();
            Review review = ReviewTestFixture.getReviewEntity();
            when(redisTemplate.executePipelined(any(RedisCallback.class))).thenReturn(null);

            // when
            feedFacade.addToFeed(followerIds, review);

            // then
            verify(redisTemplate, times(1)).executePipelined(any(RedisCallback.class));
        }
    }

    @Nested
    @DisplayName("getToFeed 테스트")
    class getToFeedTest {
        @Test
        @DisplayName("성공 - 최신 리뷰부터 조회")
        void success_latest() {
            // given
            Long memberId = 1L;
            int limit = 10;
            String userFeedKey = String.format("user:%d:feed", memberId);
            Set<Object> feedSet = FeedTestFixture.getFeedSet();

            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

            when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong()))
                    .thenReturn(feedSet);

            List<Review> reviews = FeedTestFixture.getReviews(feedSet);
            List<Long> reviewIds = reviews.stream().map(Review::getId).collect(Collectors.toList());

            when(reviewService.findByIdIn(reviewIds)).thenReturn(reviews);

            // when
            List<FeedResponseDto> feed = feedFacade.getFeeds(memberId, null, limit);

            // then
            verify(zSetOperations).reverseRange(eq(userFeedKey), eq(0L), eq((long) limit - 1));
            verify(reviewService, times(1)).findByIdIn(anyList());

            assertEquals(feedSet.size(), feed.size());
            for (Review review : reviews) {
                FeedResponseDto dto = feed.stream()
                        .filter(f -> f.getReviewId().equals(review.getId()))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Review not found in feed"));

                assertEquals(review.getId(), dto.getReviewId());
                assertEquals(review.getContent(), dto.getContent());
            }
        }

        @Test
        @DisplayName("성공 - 마지막 다음 리뷰부터 조회")
        void success_next() {
            // given
            Long memberId = 1L;
            Long lastReviewId = 10L;
            int limit = 5;
            String userFeedKey = String.format("user:%d:feed", memberId);
            Double lastReviewScore = 10.0;
            Set<Object> feedSet = FeedTestFixture.getFeedSet();

            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.score(anyString(), eq(String.valueOf(lastReviewId)))).thenReturn(lastReviewScore);
            when(zSetOperations.reverseRangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong())).thenReturn(feedSet);

            Review review1 = createMockReview(1L, "좋은 리뷰입니다!", "이순신", "다른 책 제목");
            Review review2 = createMockReview(2L, "또 다른 리뷰!", "박지성", "다른 책 제목 2");

            List<Review> reviews = List.of(review1, review2);
            List<Long> reviewIds = reviews.stream().map(Review::getId).collect(Collectors.toList());

            when(reviewService.findByIdIn(reviewIds)).thenReturn(reviews);

            // when
            List<FeedResponseDto> feed = feedFacade.getFeeds(memberId, lastReviewId, limit);

            // then
            verify(zSetOperations).reverseRangeByScore(eq(userFeedKey), eq(Double.MIN_VALUE), eq(lastReviewScore - 1), eq(0L), eq((long) limit));
            verify(reviewService).findByIdIn(eq(reviewIds));

            assertEquals(reviews.size(), feed.size());

            for (Review review : reviews) {
                FeedResponseDto dto = feed.stream().filter(f -> f.getReviewId().equals(review.getId())).findFirst().orElse(null);
                assertNotNull(dto);
                assertEquals(review.getId(), dto.getReviewId());
                assertEquals(review.getMember().getNickName(), dto.getNickName());
                assertEquals(review.getContent(), dto.getContent());
                assertEquals(review.getBook().getTitle(), dto.getBookTitle());
            }
        }
    }

    @Nested
    @DisplayName("deleteToFeed 테스트")
    class deleteToFeedTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            List<Long> followerIds = FeedTestFixture.getFollowerTestIds();
            Review review = ReviewTestFixture.getReviewEntity();
            Long reviewId = review.getId();
            when(redisTemplate.executePipelined(any(RedisCallback.class))).thenReturn(null);

            // When
            feedFacade.deleteToFeed(followerIds, reviewId);

            // Then
            verify(redisTemplate, times(1)).executePipelined(any(RedisCallback.class));
        }
    }

    private Review createMockReview(Long id, String content, String nickName, String bookTitle) {
        return Review.builder()
                .id(id)
                .content(content)
                .member(Member.builder().nickName(nickName).build())
                .book(Book.builder().title(bookTitle).build())
                .build();
    }
}