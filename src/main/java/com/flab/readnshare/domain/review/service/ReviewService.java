package com.flab.readnshare.domain.review.service;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.ReviewSearchResponseDto;
import com.flab.readnshare.domain.review.dto.UpdateReviewRequestDto;
import com.flab.readnshare.domain.review.repository.ReviewRepository;
import com.flab.readnshare.global.common.exception.MemberException;
import com.flab.readnshare.global.common.exception.ReviewException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@EnableRetry
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(ReviewException.ReviewNotFoundException::new);
    }

    public Long save(Review review) {
        return reviewRepository.save(review).getId();
    }

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class}
            , maxAttempts = 3
            , backoff = @Backoff(delay = 1000)
    )
    @Transactional
    public Long update(Long reviewId, Member signInMember, UpdateReviewRequestDto dto) {
        Review review = reviewRepository.findByIdForUpdate(reviewId).orElseThrow();
        review.verifyMember(signInMember);

        review.update(dto.getContent());

        return review.getId();
    }

    @Recover
    public Long recover(ObjectOptimisticLockingFailureException ex, Long reviewId, Member signInMember, UpdateReviewRequestDto dto) {
        log.error("review update failed... error: {}", ex.getMessage());
        throw ex;
    }

    @Transactional
    public void delete(Long reviewId, Member signInMember) {
        Review review = findById(reviewId);
        review.verifyMember(signInMember);

        reviewRepository.delete(review);
    }

    public List<Review> findByIdIn(List<Long> reviewIds) {
        return reviewRepository.findByIdIn(reviewIds);
    }


    /**************************
     추가 기능 < 2025.3.16>

     책 제목으로 리뷰 검색
     책 작가로 리뷰 검색
     책 출판사로 리뷰 검색
     리뷰 작성자 이름으로 리뷰 검색
     + 키워드 검색

    **************************/


    @Transactional(readOnly = true)
    public List<ReviewSearchResponseDto> searchByBookTitle(String title) {
        return reviewRepository.findByBook_TitleContaining(title)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewSearchResponseDto> searchByBookAuthor(String author) {
        return reviewRepository.findByBook_AuthorContaining(author)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewSearchResponseDto> searchByBookPublisher(String publisher) {
        return reviewRepository.findByBook_PublisherContaining(publisher)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewSearchResponseDto> searchByMemberName(String memberName) {
        return reviewRepository.findByMember_NameContaining(memberName)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewSearchResponseDto> searchByKeyword(String keyword) {
        return reviewRepository.searchByKeyword(keyword)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    private ReviewSearchResponseDto convertToDto(Review review){
        return new ReviewSearchResponseDto(
                review.getId(),
                review.getContent(),
                review.getBook().getTitle(),
                review.getBook().getAuthor(),
                review.getBook().getPublisher(),
                review.getMember().getNickName()
        );
    }
}
