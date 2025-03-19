package com.flab.readnshare.domain.review.service;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.UpdateReviewRequestDto;
import com.flab.readnshare.domain.review.repository.ReviewRepository;
import com.flab.readnshare.global.common.exception.ReviewException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Nested
@DisplayName("리뷰 서비스 테스트")
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BookRepository bookRepository;

    private Member member;
    private Book book;
    private Review review;



    @BeforeEach
    void setUp(){
        member = Member.builder()
                .id(null)
                .nickName("testUser")
                .build();

        book = Book.builder()
                .id(null)
                .title("Test Book")
                .author("Test Author")
                .publisher("Test Publisher")
                .isbn("1234567890")
                .build();

        review = Review.builder()
                .id(null)
                .content("My First Review!")
                .member(member)
                .book(book)
                .build();
    }


    @Nested
    @DisplayName("리뷰 저장 테스트")
    class saveTest {
        @Test
        @DisplayName("리뷰 저장 성공")
        void success() {
            // given
            when(reviewRepository.save(any(Review.class))).thenReturn(
                    Review.builder()
                            .id(1L)
                            .content("My First Review!")
                            .member(member)
                            .book(book)
                            .build()
            );

            // when
            Long savedReviewId = reviewService.save(review);

            // then
            assertThat(savedReviewId).isEqualTo(1L);
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

    }


    @Nested
    @DisplayName("리뷰 ID로 조회 테스트")
    class findByIdTest {
        @Test
        @DisplayName("리뷰 ID로 조회 성공")
        void success() {
            // given
            when(reviewRepository.findById(any(Long.class))).thenReturn(Optional.of(review));

            // when
            Review result = reviewService.findById(1L);

            // then
            assertEquals(review, result);
        }

        @Test
        @DisplayName("리뷰 ID로 조회 실패 - ID 없음")
        void find_review_fail_no_id() {
            // given
            when(reviewRepository.findById(any(Long.class))).thenReturn(Optional.empty());

            // when & then
            assertThrows(ReviewException.ReviewNotFoundException.class, () -> reviewService.findById(1L));
        }
    }


    @Nested
    @DisplayName("update 테스트")
    class updateTest {
        @Test
        @DisplayName("독서 기록 내용을 수정하면 reviewId를 반환한다")
        void success() {
            // given
            UpdateReviewRequestDto request = ReviewTestFixture.getUpdateReviewRequestDto();
            Review existReview = ReviewTestFixture.getReviewEntity();

            when(reviewRepository.findByIdForUpdate(any(Long.class))).thenReturn(Optional.of(existReview));

            // when
            Long updatedReviewId = reviewService.update(existReview.getId(), existReview.getMember(), request);

            // then
            assertEquals(existReview.getId(), updatedReviewId);
            assertEquals(existReview.getContent(), request.getContent());
        }

        @Test
        @DisplayName("독서 기록 작성자와 수정자가 다르면 예외가 발생한다")
        void fail_mismatch_member() {
            // given
            UpdateReviewRequestDto request = ReviewTestFixture.getUpdateReviewRequestDto();
            Review existReview = ReviewTestFixture.getReviewEntity();

            when(reviewRepository.findByIdForUpdate(any(Long.class))).thenReturn(Optional.of(existReview));

            assertThrows(ReviewException.ForbiddenMemberException.class
                    , () -> reviewService.update(existReview.getId(), mock(Member.class), request));
        }
    }






    @Test
    @DisplayName("독서 기록 내용을 삭제한다")
    void delete_review_success() {
        // given
        Review existReview = ReviewTestFixture.getReviewEntity();

        when(reviewRepository.findById(any(Long.class))).thenReturn(Optional.of(existReview));

        // when
        reviewService.delete(existReview.getId(), existReview.getMember());

        // then
        verify(reviewRepository, times(1)).delete(existReview);
    }

    @Test
    @DisplayName("삭제 요청자가 리뷰 작성자와 불일치")
    void delete_review_fail_mismatch_member() {
        // given
        Review existReview = ReviewTestFixture.getReviewEntity();
        Member wrongMember = Member.builder()
                .id(99L)
                .nickName("wrongUser")
                .build();
        when(reviewRepository.findById(any(Long.class))).thenReturn(Optional.of(existReview));

        // when & then
        assertThrows(ReviewException.ForbiddenMemberException.class
                , () -> reviewService.delete(existReview.getId(), wrongMember));

        // verify - delete가 호출되지 않았는지 확인
        verify(reviewRepository, never()).delete(any());

    }

}