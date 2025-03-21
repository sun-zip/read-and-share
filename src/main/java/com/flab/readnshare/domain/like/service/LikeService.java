package com.flab.readnshare.domain.like.service;

import com.flab.readnshare.domain.like.domain.Like;
import com.flab.readnshare.domain.like.event.LikeEvent;
import com.flab.readnshare.domain.like.repository.LikeRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.repository.ReviewRepository;
import com.flab.readnshare.global.common.exception.ReviewException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

	private final LikeRepository likeRepository;
	private final ReviewRepository reviewRepository;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public void toggleLike(Long reviewId, Member signInMember) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(ReviewException.ReviewNotFoundException::new);

		Optional<Like> existingLike = likeRepository.findByMemberAndReview(signInMember, review);

		if (existingLike.isPresent()) {
			// 이미 좋아요 누름 -> 취소
			likeRepository.delete(existingLike.get());
		} else {
			// 좋아요 누름
			Like newLike = Like.builder()
					.fromMember(signInMember)
					.toReview(review)
					.build();
			likeRepository.save(newLike);

			// 이벤트 발행
			eventPublisher.publishEvent(new LikeEvent(this, signInMember, review));

		}
	}
}