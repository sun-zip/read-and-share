package com.flab.readnshare.domain.review.facade;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.service.BookService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.SaveReviewRequestDto;
import com.flab.readnshare.domain.review.event.ReviewCreateEvent;
import com.flab.readnshare.domain.review.event.ReviewDeleteEvent;
import com.flab.readnshare.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReviewFacadeTest {
    @Mock
    private ReviewService reviewService;
    @Mock
    private BookService bookService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewFacade reviewFacade;

    @Test
    @DisplayName("독서 기록을 작성하면 review가 등록된다")
    void save_review_success() {
        // given
        SaveReviewRequestDto request = ReviewTestFixture.getSaveReviewRequestDto();
        Review expectedReview = ReviewTestFixture.getReviewEntity();

        when(reviewService.save(any(Review.class))).thenReturn(expectedReview.getId());

        // when
        Long savedReviewId = reviewFacade.save(request, mock(Member.class));

        // then
        assertNotNull(savedReviewId);
        assertEquals(1L, savedReviewId);
        verify(reviewService, times(1)).save(any(Review.class));
        verify(eventPublisher).publishEvent(any(ReviewCreateEvent.class));
    }

    @DisplayName("독서 기록을 삭제하면 review가 삭제된다.")
    @Test
    void delete_review_success() {
        // Given
        Member member = ReviewTestFixture.getMemberEntity();
        Review review = ReviewTestFixture.getReviewEntity();
        Long reviewId = review.getId();

        // When
        reviewFacade.delete(reviewId, member);

        // Then
        verify(reviewService, times(1)).delete(anyLong(), any(Member.class));
        verify(eventPublisher, times(1)).publishEvent(any(ReviewDeleteEvent.class));
    }

}