package com.flab.readnshare.domain.likeit.repository;

import com.flab.readnshare.domain.likeit.domain.LikeIt;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeItRepository extends JpaRepository<LikeIt, Long> {
	Optional<LikeIt> findByFromMemberAndToReview(Member fromMember, Review toReview);
}
