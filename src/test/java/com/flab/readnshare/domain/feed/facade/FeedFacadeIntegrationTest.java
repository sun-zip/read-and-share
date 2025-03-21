package com.flab.readnshare.domain.feed.facade;

import com.flab.readnshare.FeedTestFixture;
import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.feed.dto.FeedResponseDto;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.service.ReviewService;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    RedisTemplate<String, Object> feedRedisTemplate;

    @AfterEach
    void tearDown() {
        feedRedisTemplate.delete(feedRedisTemplate.keys("user:*:feed"));
    }

    @Nested
    @DisplayName("addToFeed 테스트")
    class addToFeedTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            List<Long> followerIds = FeedTestFixture.getFollowerTestIds();
            Review review = ReviewTestFixture.getReviewEntity();
            Long reviewId = review.getId();
            Long timestamp = System.currentTimeMillis();

            // When
            feedFacade.addToFeed(followerIds, review);

            // Then
            for (Long followerId : followerIds) {
                String userFeedKey = String.format("user:%d:feed", followerId);
                Set<Object> result = feedRedisTemplate.opsForZSet().range(userFeedKey, 0, -1);
                assertThat(result).hasSize(1).contains(String.valueOf(reviewId));
            }
        }
    }

    @Nested
    @DisplayName("getToFeed 테스트")
    class getToFeedTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Book book = createBook();
            bookRepository.save(book);
            Member member = createMember();
            memberRepository.save(member);
            Review review = createReview(member, book);


            List<Long> followerIds = FeedTestFixture.getFollowerTestIds();
            Long reviewId = reviewService.save(review);
            Long timestamp = System.currentTimeMillis();
            int limit = 10;

            for (Long followerId : followerIds) {
                String userFeedKey = String.format("user:%d:feed", followerId);
                feedRedisTemplate.opsForZSet().add(userFeedKey, String.valueOf(reviewId), timestamp);
            }

            // When
            List<FeedResponseDto> result = feedFacade.getFeeds(followerIds.get(0), null, limit);

            // Then
            assertThat(result).hasSize(1)
                    .extracting("reviewId", "nickName", "content", "bookTitle")
                    .containsExactly(Tuple.tuple(reviewId, member.getNickName(), review.getContent(), book.getTitle()));
        }

        @Test
        @DisplayName("성공 - 점수없음")
        void success_no_score() throws Exception {
            // Given
            List<Long> followerIds = FeedTestFixture.getFollowerTestIds();

            // When
            List<FeedResponseDto> result = feedFacade.getFeeds(followerIds.get(0), 999L, 10);

            // Then
            assertThat(result).hasSize(0).isEmpty();
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

    private Book createBook() {
        return Book.builder()
                .title("test")
                .isbn("test")
                .build();
    }

    private Review createReview(Member member, Book book) {
        return Review.builder()
                .member(member)
                .book(book)
                .content("test")
                .build();
    }

    private Member createMember() {
        return Member.builder()
                .email("test")
                .nickName("test")
                .build();
    }
}
