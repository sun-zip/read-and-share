package com.flab.readnshare.domain.review.event;

import com.flab.readnshare.domain.feed.facade.FeedFacade;
import com.flab.readnshare.domain.follow.service.FollowService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewEventListenerTest {
	@InjectMocks
	private ReviewEventListener reviewEventListener;

	@Mock
	private FollowService followService;

	@Mock
	private FeedFacade feedFacade;

	private Member writer;
	private Review review;
	private List<Long> followerIds;

	@BeforeEach
	void setUp() {
		writer = mock(Member.class);
		review = mock(Review.class);
		followerIds = List.of(1L, 2L, 3L);
	}

	@Nested
	@DisplayName("리뷰 생성 시 팔로워 피드 업데이트 테스트")
	class UpdateFollowersFeedTest {
		@Test
		@DisplayName("리뷰 생성 시 팔로워 피드에 추가된다")
		void success_add_review_to_feed() {
			// given
			ReviewCreateEvent event = new ReviewCreateEvent(writer, review);

			given(followService.getFollowerIds(writer)).willReturn(followerIds);

			// when
			reviewEventListener.updateFollowersFeed(event);

			// then
			verify(feedFacade, times(1)).addToFeed(followerIds, review);
		}

		@Test
		@DisplayName("리뷰 생성 시 팔로워가 없으면 피드 추가는 발생하지 않는다")
		void not_run_if_no_follower() {
			// given
			ReviewCreateEvent event = new ReviewCreateEvent(writer, review);

			given(followService.getFollowerIds(writer)).willReturn(List.of());

			// when
			reviewEventListener.updateFollowersFeed(event);

			// then
			verify(feedFacade, never()).addToFeed(anyList(), any());
		}

	}

	@Nested
	@DisplayName("리뷰 생성 시 팔로워 피드 업데이트 테스트")
	class DeleteReviewsFromFeedTest {

		@Test
		@DisplayName("리뷰 삭제 시 팔로워 피드에서 삭제된다")
		void success_deleted_from_feeds() {
			// given
			Long reviewId = 42L;
			ReviewDeleteEvent event = new ReviewDeleteEvent(writer, reviewId);

			given(followService.getFollowerIds(writer)).willReturn(followerIds);

			// when
			reviewEventListener.deleteReviewFromFeed(event);

			// then
			verify(feedFacade, times(1)).deleteToFeed(followerIds, reviewId);
		}

		@Test
		@DisplayName("리뷰 삭제 시 팔로워가 없으면 삭제 동작은 발생하지 않는다")
		void not_run_if_no_follower() {
			// given
			Long reviewId = 42L;
			ReviewDeleteEvent event = new ReviewDeleteEvent(writer, reviewId);

			given(followService.getFollowerIds(writer)).willReturn(List.of());

			// when
			reviewEventListener.deleteReviewFromFeed(event);

			// then
			verify(feedFacade, never()).deleteToFeed(anyList(), any());
		}
	}
}
