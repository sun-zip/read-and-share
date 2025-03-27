package com.flab.readnshare.domain.likeit.controller;

import com.flab.readnshare.domain.likeit.service.LikeItService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.resolver.SignInMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Tag(name = "Like", description = "좋아요")
public class LikeItController {

	private final LikeItService likeItService;

	@PostMapping("/{reviewId}/likes")
	@Operation(summary = "리뷰 좋아요/취소", description = "리뷰에 좋아요를 누르거나 이미 누른 좋아요를 취소합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "리뷰 좋아요/취소 성공"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
	})
	public ResponseEntity<Void> toggleLikeIt(
			@Parameter(description = "삭제할 리뷰 ID") @PathVariable Long reviewId,
			@Parameter(hidden = true) @SignInMember Member signInMember){
		likeItService.toggleLikeIt(reviewId, signInMember);
		return ResponseEntity.ok().build();
	}

}
