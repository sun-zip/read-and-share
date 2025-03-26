package com.flab.readnshare.domain.review.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.dto.ReviewSearchCondition;
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
@RequestMapping("/api/v1/reviews")
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


    @GetMapping
    public ResponseEntity<List<ReviewSearchResponseDto>> search(@ModelAttribute ReviewSearchCondition condition) {
        if (condition.countNonEmpty() != 1) {
            throw new IllegalArgumentException("검색 조건은 하나만 입력해야 합니다.");
        }

        if (condition.getTitle() != null) return ResponseEntity.ok(reviewService.searchByBookTitle(condition.getTitle()));
        if (condition.getAuthor() != null) return ResponseEntity.ok(reviewService.searchByBookAuthor(condition.getAuthor()));
        if (condition.getPublisher() != null) return ResponseEntity.ok(reviewService.searchByBookPublisher(condition.getPublisher()));
        if (condition.getMemberName() != null) return ResponseEntity.ok(reviewService.searchByMemberNickName(condition.getMemberName()));
        if (condition.getKeyword() != null) return ResponseEntity.ok(reviewService.searchByKeyword(condition.getKeyword()));

        throw new IllegalArgumentException("올바른 검색 조건이 없습니다.");
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

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewSearchResponseDto> findById(@PathVariable Long reviewId) {
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

    */

}
