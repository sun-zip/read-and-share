package com.flab.readnshare.domain.likeit.service;

import com.flab.readnshare.domain.likeit.domain.LikeIt;
import com.flab.readnshare.domain.likeit.event.LikeItEvent;
import com.flab.readnshare.domain.likeit.repository.LikeItRepository;
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
public class LikeItService {

	private final LikeItRepository likeItRepository;
	private final ReviewRepository reviewRepository;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public void toggleLikeIt(Long reviewId, Member signInMember) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(ReviewException.ReviewNotFoundException::new);

		Optional<LikeIt> existingLikeIt = likeItRepository.findByFromMemberAndToReview(signInMember, review);

		if (existingLikeIt.isPresent()) {
			// 이미 좋아요 누름 -> 취소
			likeItRepository.delete(existingLikeIt.get());
		} else {
			// 좋아요 누름
			LikeIt newLikeIt = LikeIt.builder()
					.fromMember(signInMember)
					.toReview(review)
					.build();
			likeItRepository.save(newLikeIt);

			// 이벤트 발행
			eventPublisher.publishEvent(new LikeItEvent(this, signInMember, review));

		}
	}
}