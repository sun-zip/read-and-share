package com.flab.readnshare.domain.like.controller;

import com.flab.readnshare.domain.like.service.LikeService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.resolver.SignInMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class LikeController {

	private final LikeService likeService;

	@PostMapping("/{reviewId}/like")
	public ResponseEntity<Void> toggleLike(@PathVariable Long reviewId, @SignInMember Member signInMember){
		likeService.toggleLike(reviewId, signInMember);
		return ResponseEntity.ok().build();
	}

}
