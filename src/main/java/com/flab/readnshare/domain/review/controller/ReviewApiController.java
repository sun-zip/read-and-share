package com.flab.readnshare.domain.review.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.ReviewSearchCondition;
import com.flab.readnshare.domain.review.dto.ReviewSearchResponseDto;
import com.flab.readnshare.domain.review.dto.SaveReviewRequestDto;
import com.flab.readnshare.domain.review.dto.UpdateReviewRequestDto;
import com.flab.readnshare.domain.review.facade.ReviewFacade;
import com.flab.readnshare.domain.review.service.ReviewService;
import com.flab.readnshare.global.common.resolver.SignInMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Tag(name = "Review", description = "리뷰")
public class ReviewApiController {
    private final ReviewService reviewService;
    private final ReviewFacade reviewFacade;

	@PostMapping
	@Operation(summary = "리뷰 등록", description = "리뷰를 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	})
    public ResponseEntity<Long> save(@Valid @RequestBody SaveReviewRequestDto dto, @SignInMember Member signInMember) {
        Long reviewId = reviewFacade.save(dto, signInMember);
        return new ResponseEntity<>(reviewId, HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
	@Operation(summary = "리뷰 수정", description = "리뷰를 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "403", description = "수정 권한 없음"),
			@ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
	})
    public ResponseEntity<Long> update(
            @PathVariable Long reviewId
            , @SignInMember Member signInMember
            , @Valid @RequestBody UpdateReviewRequestDto dto) {
        return new ResponseEntity<>(reviewService.update(reviewId, signInMember, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
	@Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
			@ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
	})
    public ResponseEntity<Void> delete(
			@Parameter(description = "삭제할 리뷰 ID") @PathVariable Long reviewId,
			@Parameter(hidden = true) @SignInMember Member signInMember) {
        reviewFacade.delete(reviewId, signInMember);
        return new ResponseEntity<>(HttpStatus.OK);
    }

	@GetMapping("/{reviewId}")
	@Operation(summary = "리뷰 단건 조회", description = "리뷰 ID를 기반으로 리뷰 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
			@ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
	})
	public ResponseEntity<ReviewSearchResponseDto> findById(
			@Parameter(description = "조회할 리뷰 ID") @PathVariable Long reviewId) {
		Review review = reviewService.findById(reviewId);
		ReviewSearchResponseDto dto = new ReviewSearchResponseDto(
				review.getId(),
				review.getContent(),
				review.getBook().getTitle(),
				review.getBook().getAuthor(),
				review.getBook().getPublisher(),
				review.getMember().getNickName()
		);
		return ResponseEntity.ok(dto);
	}

    @GetMapping
	@Operation(
			summary = "리뷰 검색",
			description = """
        등록된 리뷰를 다양한 키워드(책 제목, 저자, 출판사, 작성자 닉네임, 키워드) 중 하나로 검색합니다.
        ⚠️ 단, 검색 조건은 **하나만** 입력해야 합니다.
        """
	)
    public ResponseEntity<List<ReviewSearchResponseDto>> search(
			@ParameterObject @ModelAttribute ReviewSearchCondition condition) {
        if (condition.countNonEmpty() != 1) {
            throw new IllegalArgumentException("검색 조건은 하나만 입력해야 합니다.");
        }

		return switch (condition.inputArgument()) {
			case "title" -> ResponseEntity.ok(reviewService.searchByBookTitle(condition.getTitle()));
			case "author" -> ResponseEntity.ok(reviewService.searchByBookAuthor(condition.getAuthor()));
			case "publisher" -> ResponseEntity.ok(reviewService.searchByBookPublisher(condition.getPublisher()));
			case "memberName" -> ResponseEntity.ok(reviewService.searchByMemberNickName(condition.getMemberName()));
			default -> ResponseEntity.ok(reviewService.searchByKeyword(condition.getKeyword()));
		};
    }


    /*


    // 책 제목으로 검색
    @GetMapping("/search/title")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByTitle(@RequestParam String title) {
        if(title.isBlank()){
            throw new IllegalArgumentException("검색어를 입력하세요.");
        }
        return new ResponseEntity<>(reviewService.searchByBookTitle(title), HttpStatus.OK);
    }

    // 저자명으로 검색
    @GetMapping("/search/author")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByAuthor(@RequestParam String author) {
        if(author.isBlank()){
            throw new IllegalArgumentException("검색어를 입력하세요.");
        }
        return new ResponseEntity<>(reviewService.searchByBookAuthor(author), HttpStatus.OK);
    }

    // 출판사로 검색
    @GetMapping("/search/publisher")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByPublisher(@RequestParam String publisher) {
        if(publisher.isBlank()){
            throw new IllegalArgumentException("검색어를 입력하세요.");
        }
        return new ResponseEntity<>(reviewService.searchByBookPublisher(publisher), HttpStatus.OK);
    }

    // 작성자 이름으로 검색
    @GetMapping("/search/member")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByMemberName(@RequestParam String memberName) {
        if(memberName.isBlank()){
            throw new IllegalArgumentException("검색어를 입력하세요.");
        }
        return new ResponseEntity<>(reviewService.searchByMemberNickName(memberName), HttpStatus.OK);

    }

    // 키워드 검색
    @GetMapping("/search/keyword")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByKeyword(@RequestParam String keyword) {
        if(keyword.isBlank()){
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
        return new ResponseEntity<>(reviewService.searchByKeyword(keyword), HttpStatus.OK);
    }

    */

}
