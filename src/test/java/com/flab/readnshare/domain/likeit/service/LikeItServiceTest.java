package com.flab.readnshare.domain.likeit.service;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.likeit.domain.LikeIt;
import com.flab.readnshare.domain.likeit.event.LikeItEvent;
import com.flab.readnshare.domain.likeit.repository.LikeItRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.repository.ReviewRepository;
import com.flab.readnshare.global.common.exception.ReviewException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Nested
public class LikeItServiceTest {

	@InjectMocks
	private LikeItService likeItService;

	@Mock
	private LikeItRepository likeItRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private Member fromMember;
	private Member toMember;
	private Review toReview;


	@BeforeEach
	void setUp(){
		fromMember = Member.builder()
				.id(1L)
				.nickName("tester")
				.build();
		toMember = Member.builder()
				.id(2L)
				.nickName("testReviewer")
				.build();
		toReview = Review.builder()
				.id(1L)
				.content("Test review content")
				.member(toMember)
				.book(mock(Book.class))
				.score(10)
				.build();
	}

	@Nested
	@DisplayName("좋아요 토글 테스트")
	class toggleLikeItTest {

		@Test
		@DisplayName("좋아요 저장, 이벤트 발행")
		void success_save(){
			// given
			when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(toReview));
			when(likeItRepository.findByFromMemberAndToReview(any(Member.class), any(Review.class))).thenReturn(Optional.empty()); // 항상 검색이 안되도록 세팅

			// when
			likeItService.toggleLikeIt(toReview.getId(), fromMember);

			// then
			verify(likeItRepository, times(1)).save(any(LikeIt.class));
			verify(eventPublisher, times(1)).publishEvent(any(LikeItEvent.class));
			verify(likeItRepository, never()).delete(any(LikeIt.class));
		}

		@Test
		@DisplayName("좋아요 삭제")
		void success_delete(){
			// given
			LikeIt existingLikeIt = LikeIt.builder()
					.fromMember(fromMember)
					.toReview(toReview)
					.build();
			// ReflectionTestUtils.setField(existingLike, "id", 1L); // ID값을 넣어줘야하나 해서 썼는데 안써도 될듯싶음

			when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(toReview));
			when(likeItRepository.findByFromMemberAndToReview(any(Member.class), any(Review.class))).thenReturn(Optional.of(existingLikeIt)); // 항상 검색이 되도록 세팅

			// when
			likeItService.toggleLikeIt(1L, fromMember);

			// then
			verify(likeItRepository, never()).save(any(LikeIt.class));
			verify(eventPublisher, never()).publishEvent(any(LikeItEvent.class));
			verify(likeItRepository, times(1)).delete(any(LikeIt.class));

		}

		@Test
		@DisplayName("좋아요 실패 - 존재하지 않는 ID")
		void fail_id_not_found(){

			// given
			when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

			// when & then
			assertThrows(ReviewException.ReviewNotFoundException.class, () -> likeItService.toggleLikeIt(1L, fromMember));
			verify(likeItRepository, never()).delete(any(LikeIt.class));
			verify(likeItRepository, never()).save(any(LikeIt.class));
			verify(eventPublisher, never()).publishEvent(any(LikeItEvent.class));
		}

	}



}
