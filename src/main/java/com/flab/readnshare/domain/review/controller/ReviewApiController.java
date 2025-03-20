package com.flab.readnshare.domain.review.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.dto.ReviewSearchResponseDto;
import com.flab.readnshare.domain.review.dto.SaveReviewRequestDto;
import com.flab.readnshare.domain.review.dto.UpdateReviewRequestDto;
import com.flab.readnshare.domain.review.facade.ReviewFacade;
import com.flab.readnshare.domain.review.service.ReviewService;
import com.flab.readnshare.global.common.resolver.SignInMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewApiController {
    private final ReviewService reviewService;
    private final ReviewFacade reviewFacade;

    @PostMapping
    public ResponseEntity<Long> save(@Valid @RequestBody SaveReviewRequestDto dto, @SignInMember Member signInMember) {
        Long reviewId = reviewFacade.save(dto, signInMember);
        return new ResponseEntity<>(reviewId, HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Long> update(
            @PathVariable Long reviewId
            , @SignInMember Member signInMember
            , @Valid @RequestBody UpdateReviewRequestDto dto) {
        return new ResponseEntity<>(reviewService.update(reviewId, signInMember, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId, @SignInMember Member signInMember) {
        reviewFacade.delete(reviewId, signInMember);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**************************
     추가 기능 < 2025.3.16>

     책 제목으로 리뷰 검색
     책 작가로 리뷰 검색
     책 출판사로 리뷰 검색
     리뷰 작성자 이름으로 리뷰 검색
     + 키워드 검색

     **************************/


    // 책 제목으로 검색
    @GetMapping("/search/title")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByTitle(@RequestParam String title) {
        return new ResponseEntity<>(reviewService.searchByBookTitle(title), HttpStatus.OK);
    }

    // 저자명으로 검색
    @GetMapping("/search/author")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByAuthor(@RequestParam String author) {
        return new ResponseEntity<>(reviewService.searchByBookAuthor(author), HttpStatus.OK);
    }

    // 출판사로 검색
    @GetMapping("/search/publisher")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByPublisher(@RequestParam String publisher) {
        return new ResponseEntity<>(reviewService.searchByBookPublisher(publisher), HttpStatus.OK);
    }

    // 작성자 이름으로 검색
    @GetMapping("/search/member")
    public ResponseEntity<List<ReviewSearchResponseDto>> searchByMemberName(@RequestParam String memberName) {
        return new ResponseEntity<>(reviewService.searchByMemberNickName(memberName), HttpStatus.OK);

    }

}
