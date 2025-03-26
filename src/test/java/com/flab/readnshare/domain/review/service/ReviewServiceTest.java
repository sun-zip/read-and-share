package com.flab.readnshare.domain.review.service;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.ReviewSearchResponseDto;
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

import java.util.List;
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
    @DisplayName("여러 ID로 조회 테스트")
    class findByIdInTest {
        @Test
        @DisplayName("여러개의 ID로 조회 성공")
        void success() {
            // given
            Review review1 = Review.builder()
                    .id(1L)
                    .content("My First Review!")
                    .member(member)
                    .book(book)
                    .build();
            Review review2 = Review.builder()
                    .id(2L)
                    .content("My Second Review!")
                    .member(member)
                    .book(book)
                    .build();
            Review review3 = Review.builder()
                    .id(3L)
                    .content("My Third Review!")
                    .member(member)
                    .book(book)
                    .build();

            when(reviewRepository.findByIdIn(any(List.class))).thenReturn(List.of(review1, review2, review3));

            // when
            List<Review> result = reviewService.findByIdIn(List.of(1L, 2L, 3L));

            // then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("리뷰 ID로 조회 실패 - ID 없음")
        void find_review_fail_no_id() {
            // given
            when(reviewRepository.findByIdIn(any(List.class))).thenReturn(List.of());

            // when & then
            assertThat(reviewService.findByIdIn(List.of(1L,2L,3L))).isEmpty();
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
        @DisplayName("내용만 수정, 별점 유지")
        void content_only() {
            // given
            UpdateReviewRequestDto request = new UpdateReviewRequestDto("Updated content", null);
            Review existReview = ReviewTestFixture.getReviewEntity();
            existReview.updateScore(7); // 초기 score: 7점으로 세팅

            when(reviewRepository.findByIdForUpdate(any(Long.class))).thenReturn(Optional.of(existReview));

            // when
            reviewService.update(existReview.getId(), existReview.getMember(), request);

            // then
            assertEquals("Updated content", existReview.getContent());
            assertEquals(7, existReview.getScore()); // score 그대로 유지 확인
        }

        @Test
        @DisplayName("내용과 별점 수정")
        void content_and_score() {
            // given
            Integer newScore = 8;
            UpdateReviewRequestDto request = new UpdateReviewRequestDto("Updated content", newScore);
            Review existReview = ReviewTestFixture.getReviewEntity();
            existReview.updateScore(7); // 초기 score: 7점으로 세팅

            when(reviewRepository.findByIdForUpdate(any(Long.class))).thenReturn(Optional.of(existReview));

            // when
            Long updatedReviewId = reviewService.update(existReview.getId(), existReview.getMember(), request);

            // then
            assertThat(existReview.getScore()).isEqualTo(newScore);
            assertThat(existReview.getContent()).isEqualTo(request.getContent());
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

        @Test
        @DisplayName("수정하려는 회원이 null이면 예외가 발생한다.")
        void fail_null_member_access() {
            // given
            UpdateReviewRequestDto request = ReviewTestFixture.getUpdateReviewRequestDto();
            Review existReview = ReviewTestFixture.getReviewEntity();

            when(reviewRepository.findByIdForUpdate(any(Long.class))).thenReturn(Optional.of(existReview));

            assertThrows(ReviewException.ForbiddenMemberException.class
                    , () -> reviewService.update(existReview.getId(), null, request));
        }

        public static class FakeMember extends Member {}

        @Test
        @DisplayName("수정하려는 회원에 다른 데이터가 들어가면 예외가 발생한다.")
        void fail_unavailable_member_access() {
            // given
            UpdateReviewRequestDto request = ReviewTestFixture.getUpdateReviewRequestDto();
            Review existReview = ReviewTestFixture.getReviewEntity();

            when(reviewRepository.findByIdForUpdate(any(Long.class))).thenReturn(Optional.of(existReview));

            assertThrows(ReviewException.ForbiddenMemberException.class
                    , () -> reviewService.update(existReview.getId(), mock(FakeMember.class), request));
        }

        @Test
        @DisplayName("해당 ID로 찾을 수 없음")
        void fail_id_not_found() {
            // given
            UpdateReviewRequestDto request = ReviewTestFixture.getUpdateReviewRequestDto();
            when(reviewRepository.findByIdForUpdate(any(Long.class))).thenReturn(Optional.empty());

            // when & then
            assertThrows(ReviewException.ReviewNotFoundException.class
                    , () -> reviewService.update(1L, member, request));
        }
    }

    @Nested
    @DisplayName("delete 테스트")
    class deleteTest {
        @Test
        @DisplayName("독서 기록 내용을 삭제한다")
        void success() {
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
        void fail_mismatch_member() {
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

        @Test
        @DisplayName("해당 ID로 찾을 수 없음")
        void fail_id_not_found() {
            // given
            when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            assertThrows(ReviewException.ReviewNotFoundException.class
                    , () -> reviewService.delete(1L, member));

            // verify
            verify(reviewRepository, never()).delete(any());
        }
    }


    @Nested
    @DisplayName("search by book title 테스트")
    class searchByBookTitleTest {
        @Test
        @DisplayName("책 제목으로 리뷰 검색 성공")
        void success() {
            // given
            String keyword = "some keyword";
            Review review1 = Review.builder()
                    .id(1L)
                    .content("Good book about Java!")
                    .member(member)
                    .book(book)
                    .build();

            Review review2 = Review.builder()
                    .id(2L)
                    .content("Learn Java step by step")
                    .member(member)
                    .book(book)
                    .build();

            when(reviewRepository.findByBook_TitleContaining(keyword)).thenReturn(List.of(review1, review2));

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByBookTitle("some keyword");

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.get(0).getContent()).isEqualTo("Good book about Java!");
            assertThat(result.get(1).getReviewId()).isEqualTo(2L);
            assertThat(result.get(1).getContent()).isEqualTo("Learn Java step by step");
            verify(reviewRepository, times(1)).findByBook_TitleContaining(keyword);
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void emptyResult() {
            // given
            String keyword = "NonExistingKeyword";
            when(reviewRepository.findByBook_TitleContaining(keyword)).thenReturn(List.of());

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByBookTitle(keyword);

            // then
            assertThat(result).isEmpty();
            verify(reviewRepository, times(1)).findByBook_TitleContaining(keyword);
        }

    }

    @Nested
    @DisplayName("search by book author 테스트")
    class searchByBookAuthorTest {
        @Test
        @DisplayName("책 저자로 리뷰 검색 성공")
        void success() {
            // given
            String keyword = "some keyword";
            Review review1 = Review.builder()
                    .id(1L)
                    .content("Good book about Java!")
                    .member(member)
                    .book(book)
                    .build();

            Review review2 = Review.builder()
                    .id(2L)
                    .content("Learn Java step by step")
                    .member(member)
                    .book(book)
                    .build();

            when(reviewRepository.findByBook_AuthorContaining(keyword)).thenReturn(List.of(review1, review2));

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByBookAuthor("some keyword");

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.get(0).getContent()).isEqualTo("Good book about Java!");
            assertThat(result.get(1).getReviewId()).isEqualTo(2L);
            assertThat(result.get(1).getContent()).isEqualTo("Learn Java step by step");
            verify(reviewRepository, times(1)).findByBook_AuthorContaining(keyword);
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void emptyResult() {
            // given
            String keyword = "NonExistingKeyword";
            when(reviewRepository.findByBook_AuthorContaining(keyword)).thenReturn(List.of());

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByBookAuthor(keyword);

            // then
            assertThat(result).isEmpty();
            verify(reviewRepository, times(1)).findByBook_AuthorContaining(keyword);
        }

    }


    @Nested
    @DisplayName("search by book publisher 테스트")
    class searchByBookPublisherTest {
        @Test
        @DisplayName("책 출판사로 리뷰 검색 성공")
        void success() {
            // given
            String keyword = "some keyword";
            Review review1 = Review.builder()
                    .id(1L)
                    .content("Good book about Java!")
                    .member(member)
                    .book(book)
                    .build();

            Review review2 = Review.builder()
                    .id(2L)
                    .content("Learn Java step by step")
                    .member(member)
                    .book(book)
                    .build();

            when(reviewRepository.findByBook_PublisherContaining(keyword)).thenReturn(List.of(review1, review2));

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByBookPublisher("some keyword");

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.get(0).getContent()).isEqualTo("Good book about Java!");
            assertThat(result.get(1).getReviewId()).isEqualTo(2L);
            assertThat(result.get(1).getContent()).isEqualTo("Learn Java step by step");
            verify(reviewRepository, times(1)).findByBook_PublisherContaining(keyword);
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void emptyResult() {
            // given
            String keyword = "NonExistingKeyword";
            when(reviewRepository.findByBook_PublisherContaining(keyword)).thenReturn(List.of());

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByBookPublisher(keyword);

            // then
            assertThat(result).isEmpty();
            verify(reviewRepository, times(1)).findByBook_PublisherContaining(keyword);
        }

    }

    @Nested
    @DisplayName("search by member nick name 테스트")
    class searchByMemberNickNameTest {
        @Test
        @DisplayName("리뷰어 이름으로 리뷰 검색 성공")
        void success() {
            // given
            String keyword = "some keyword";
            Review review1 = Review.builder()
                    .id(1L)
                    .content("Good book about Java!")
                    .member(member)
                    .book(book)
                    .build();

            Review review2 = Review.builder()
                    .id(2L)
                    .content("Learn Java step by step")
                    .member(member)
                    .book(book)
                    .build();

            when(reviewRepository.findByMember_NickNameContaining(keyword)).thenReturn(List.of(review1, review2));

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByMemberNickName("some keyword");

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.get(0).getContent()).isEqualTo("Good book about Java!");
            assertThat(result.get(1).getReviewId()).isEqualTo(2L);
            assertThat(result.get(1).getContent()).isEqualTo("Learn Java step by step");
            verify(reviewRepository, times(1)).findByMember_NickNameContaining(keyword);
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void emptyResult() {
            // given
            String keyword = "NonExistingKeyword";
            when(reviewRepository.findByMember_NickNameContaining(keyword)).thenReturn(List.of());

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByMemberNickName(keyword);

            // then
            assertThat(result).isEmpty();
            verify(reviewRepository, times(1)).findByMember_NickNameContaining(keyword);
        }

    }


    @Nested
    @DisplayName("리뷰 키워드 검색 테스트")
    class searchByKeyWordTest {

        @Test
        @DisplayName("검색어가 포함된 리뷰 목록 조회 성공")
        void success() {
            // given
            String keyword = "Java";
            Review review1 = Review.builder()
                    .id(1L)
                    .content("Good book about Java!")
                    .member(member)
                    .book(book)
                    .build();

            Review review2 = Review.builder()
                    .id(2L)
                    .content("Learn Java step by step")
                    .member(member)
                    .book(book)
                    .build();

            when(reviewRepository.searchByKeyword(keyword)).thenReturn(List.of(review1, review2));

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByKeyword(keyword);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.get(0).getContent()).isEqualTo("Good book about Java!");
            assertThat(result.get(1).getReviewId()).isEqualTo(2L);
            assertThat(result.get(1).getContent()).isEqualTo("Learn Java step by step");

            verify(reviewRepository, times(1)).searchByKeyword(keyword);
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void emptyResult() {
            // given
            String keyword = "NonExistingKeyword";
            when(reviewRepository.searchByKeyword(keyword)).thenReturn(List.of());

            // when
            List<ReviewSearchResponseDto> result = reviewService.searchByKeyword(keyword);

            // then
            assertThat(result).isEmpty();
            verify(reviewRepository, times(1)).searchByKeyword(keyword);
        }

    }



}