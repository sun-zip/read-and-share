package com.flab.readnshare.domain.like.repository;

import com.flab.readnshare.domain.like.domain.Like;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
	Optional<Like> findByMemberAndReview(Member member, Review review);
}
