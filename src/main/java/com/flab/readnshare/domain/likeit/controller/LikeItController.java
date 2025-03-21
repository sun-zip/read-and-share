package com.flab.readnshare.domain.likeit.controller;

import com.flab.readnshare.domain.likeit.service.LikeItService;
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
public class LikeItController {

	private final LikeItService likeItService;

	@PostMapping("/{reviewId}/like")
	public ResponseEntity<Void> toggleLike(@PathVariable Long reviewId, @SignInMember Member signInMember){
		likeItService.toggleLike(reviewId, signInMember);
		return ResponseEntity.ok().build();
	}

}
