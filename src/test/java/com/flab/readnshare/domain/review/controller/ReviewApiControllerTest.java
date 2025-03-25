package com.flab.readnshare.domain.review.controller;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.controller.uri.ReviewsApiUri;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.ReviewSearchResponseDto;
import com.flab.readnshare.domain.review.dto.SaveReviewRequestDto;
import com.flab.readnshare.domain.review.dto.UpdateReviewRequestDto;
import com.flab.readnshare.domain.review.facade.ReviewFacade;
import com.flab.readnshare.domain.review.service.ReviewService;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
import com.flab.readnshare.global.common.exception.ReviewException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Nested
class ReviewApiControllerTest {
    @Mock
    private ReviewService reviewService;
    @Mock
    private ReviewFacade reviewFacade;
    @InjectMocks
    private ReviewApiController reviewApiController;
    @InjectMocks
    private ApiExceptionAdvice apiExceptionAdvice;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(reviewApiController)
                .setControllerAdvice(apiExceptionAdvice)
                .build();
    }

    @Nested
    @DisplayName("독서 기록 등록 테스트")
    class saveTest {
        @Test
        @DisplayName("독서 기록 등록에 성공한다.")
        void success() throws Exception {
            // given
            SaveReviewRequestDto request = ReviewTestFixture.getSaveReviewRequestDto();

            given(reviewFacade.save(any(SaveReviewRequestDto.class), any(Member.class))).willReturn(1L);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post(ReviewsApiUri.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // then
            resultActions.andExpect(status().isCreated())
                    .andExpect(jsonPath("$").value(1));
        }

        @Test
        @DisplayName("독서 기록 등록에 실패한다. (내용 없음)")
        void fail_no_content() throws Exception {
            SaveReviewRequestDto request = mock(SaveReviewRequestDto.class);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post(ReviewsApiUri.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            resultActions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                    .andExpect(jsonPath("$.errors[0].field").value("content"))
                    .andExpect(jsonPath("$.errors[0].message").value("내용을 입력해주세요."));
        }

        @Test
        @DisplayName("독서 기록 등록에 실패한다. (책 isbn 정보 없음)")
        void fail_no_isbn() throws Exception {
            SaveReviewRequestDto request = SaveReviewRequestDto.builder()
                    .content("내용")
                    .book(BookDto.builder().id(1L).isbn("").title("테스트").build())
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post(ReviewsApiUri.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            resultActions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                    .andExpect(jsonPath("$.errors[0].field").value("book.isbn"))
                    .andExpect(jsonPath("$.errors[0].message").value("책 isbn이 없습니다."));
        }

        @Test
        @DisplayName("독서 기록 등록에 실패한다. (책 제목 정보 없음)")
        void fail_no_title() throws Exception {
            SaveReviewRequestDto request = SaveReviewRequestDto.builder()
                    .content("내용")
                    .book(BookDto.builder().id(1L).isbn("1234").title("").build())
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post(ReviewsApiUri.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            resultActions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                    .andExpect(jsonPath("$.errors[0].field").value("book.title"))
                    .andExpect(jsonPath("$.errors[0].message").value("책 제목이 없습니다."));
        }

    }

    @Nested
    @DisplayName("독서 기록 삭제 테스트")
    class deleteTest {

        @DisplayName("독서 기록 삭제에 성공한다.")
        @Test
        void success() throws Exception {
            // Given
            Review review = ReviewTestFixture.getReviewEntity();
            Long reviewId = review.getId();
            doNothing().when(reviewFacade).delete(anyLong(), any(Member.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete(ReviewsApiUri.BY_ID, reviewId)
            );

            // Then
            resultActions.andExpect(status().isOk());
        }

        @DisplayName("존재하지 않는 리뷰를 삭제하면 REVIEW_NOT_FOUND 예외가 발생한다.")
        @Test
        void fail_not_found() throws Exception {
            // Given
            Review review = ReviewTestFixture.getReviewEntity();
            Long reviewId = review.getId();
            doThrow(new ReviewException.ReviewNotFoundException()).when(reviewFacade).delete(anyLong(), any(Member.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete(ReviewsApiUri.BY_ID, reviewId)
            );

            // Then
            resultActions.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 독서 기록 입니다."));
        }

        @DisplayName("삭제 권한이 없는 사용자가 리뷰를 삭제할 경우 REVIEW_FORBIDDEN_MEMBER 에러가 발생한다.")
        @Test
        void fail_no_permission() throws Exception {
            // Given
            Review review = ReviewTestFixture.getReviewEntity();
            Long reviewId = review.getId();
            doThrow(new ReviewException.ForbiddenMemberException()).when(reviewFacade).delete(anyLong(), any(Member.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete(ReviewsApiUri.BY_ID, reviewId)
            );

            // Then
            resultActions.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("해당 독서 기록에 대한 수정 및 삭제 권한이 없습니다."));
        }


    }

    @Nested
    @DisplayName("독서 기록 수정 테스트")
    class updateTest {
        @Test
        @DisplayName("독서 기록 수정에 성공한다.")
        void success() throws Exception {
            // given
            UpdateReviewRequestDto request = new UpdateReviewRequestDto("수정된 내용", 8);

            given(reviewService.update(anyLong(), any(Member.class), any(UpdateReviewRequestDto.class))).willReturn(1L);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put(ReviewsApiUri.BY_ID, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(1));
        }

        @DisplayName("존재하지 않는 리뷰를 수정하면 REVIEW_NOT_FOUND 예외가 발생한다.")
        @Test
        void fail_not_found() throws Exception {
            // Given
            UpdateReviewRequestDto request = new UpdateReviewRequestDto("수정된 내용", 7);

            doThrow(new ReviewException.ReviewNotFoundException()).when(reviewService).update(anyLong(), any(Member.class), any(UpdateReviewRequestDto.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put(ReviewsApiUri.BY_ID, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // Then
            resultActions.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 독서 기록 입니다."));
        }

        @DisplayName("수정 권한이 없는 사용자가 리뷰를 삭제할 경우 REVIEW_FORBIDDEN_MEMBER 에러가 발생한다.")
        @Test
        void fail_no_permission() throws Exception {
            // Given
            UpdateReviewRequestDto request = new UpdateReviewRequestDto("수정된 내용", 7);

            doThrow(new ReviewException.ForbiddenMemberException()).when(reviewService).update(anyLong(), any(Member.class), any(UpdateReviewRequestDto.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put(ReviewsApiUri.BY_ID, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // Then
            resultActions.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("해당 독서 기록에 대한 수정 및 삭제 권한이 없습니다."));
        }

        @Test
        @DisplayName("독서 기록 수정에 실패한다. (내용 없음)")
        void fail_no_content() throws Exception {
            UpdateReviewRequestDto request = new UpdateReviewRequestDto("", 7);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put(ReviewsApiUri.BY_ID, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            resultActions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                    .andExpect(jsonPath("$.errors[0].field").value("content"))
                    .andExpect(jsonPath("$.errors[0].message").value("내용을 입력해주세요."));
        }
    }



    @Nested
    @DisplayName("리뷰 검색 테스트")
    class searchTest {

        @Nested
        @DisplayName("책 제목으로 리뷰 검색 테스트")
        class searchByBookTitleTest {
            @Test
            @DisplayName("책 제목으로 리뷰 검색 성공")
            void success() throws Exception {
                // given
                String title = "Java";
                List<ReviewSearchResponseDto> mockResponse = List.of(
                        new ReviewSearchResponseDto(1L, "내용1", "Java", "Author1", "Publisher1", "Nick1"),
                        new ReviewSearchResponseDto(2L, "내용2", "Java", "Author2", "Publisher2", "Nick2")
                );

                when(reviewService.searchByBookTitle(title)).thenReturn(mockResponse);

                // when
                ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.get(ReviewsApiUri.BASE)
                                .param("title", title)
                                .accept(MediaType.APPLICATION_JSON)
                );

                // then
                resultActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.size()").value(2))
                        .andExpect(jsonPath("$[0].reviewId").value(1L))
                        .andExpect(jsonPath("$[1].reviewId").value(2L));
                verify(reviewService, times(1)).searchByBookTitle(title);
            }
        }


        @Nested
        @DisplayName("책 저자명으로 리뷰 검색 테스트")
        class searchByBookAuthorTest {

            @Test
            @DisplayName("책 저자명으로 리뷰 검색 성공")
            void success() throws Exception {
                // given
                String author = "AuthorName";
                List<ReviewSearchResponseDto> mockResponse = List.of(
                        new ReviewSearchResponseDto(1L, "내용1", "Title1", author, "Publisher1", "Nick1")
                );

                when(reviewService.searchByBookAuthor(author)).thenReturn(mockResponse);

                // when
                ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.get(ReviewsApiUri.BASE)
                                .param("author", author)
                                .accept(MediaType.APPLICATION_JSON)
                );

                // then
                resultActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.size()").value(1))
                        .andExpect(jsonPath("$[0].bookAuthor").value(author));
                verify(reviewService, times(1)).searchByBookAuthor(author);
            }

        }


        @Nested
        @DisplayName("책 출판사명으로 리뷰 검색 테스트")
        class searchByBookPublisherTest {
            @Test
            @DisplayName("책 출판사로 리뷰 검색 성공")
            void success() throws Exception {
                // given
                String publisher = "TechBooks";
                List<ReviewSearchResponseDto> mockResponse = List.of(
                        new ReviewSearchResponseDto(1L, "내용1", "Title1", "Author1", publisher, "Nick1")
                );

                when(reviewService.searchByBookPublisher(publisher)).thenReturn(mockResponse);

                // when
                ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.get(ReviewsApiUri.BASE)
                                .param("publisher", publisher)
                                .accept(MediaType.APPLICATION_JSON)
                );

                // then
                resultActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.size()").value(1))
                        .andExpect(jsonPath("$[0].bookPublisher").value(publisher));
                verify(reviewService, times(1)).searchByBookPublisher(publisher);
            }
        }


        @Nested
        @DisplayName("리뷰 작성자 닉네임으로 리뷰 검색 테스트")
        class searchByMemberNickNameTest {
            @Test
            @DisplayName("리뷰 작성자 닉네임으로 리뷰 검색 성공")
            void success() throws Exception {
                // given
                String memberName = "Reviewer";
                List<ReviewSearchResponseDto> mockResponse = List.of(
                        new ReviewSearchResponseDto(1L, "내용1", "Title1", "Author1", "Publisher1", memberName)
                );

                when(reviewService.searchByMemberNickName(memberName)).thenReturn(mockResponse);

                // when
                ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.get(ReviewsApiUri.BASE)
                                .param("memberName", memberName)
                                .accept(MediaType.APPLICATION_JSON)
                );

                // then
                resultActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.size()").value(1))
                        .andExpect(jsonPath("$[0].memberName").value(memberName));
                verify(reviewService, times(1)).searchByMemberNickName(memberName);
            }
        }


        @Nested
        @DisplayName("통합 키워드 검색 테스트")
        class searchByKeywordTest{
            @Test
            @DisplayName("통합 키워드 검색에 성공한다")
            void success() throws Exception {
                // given
                String keyword = "test";

                List<ReviewSearchResponseDto> mockResponse = List.of(
                        new ReviewSearchResponseDto(1L, "test content1", "test title1", "test author1", "test publisher1", "test name 1"),
                        new ReviewSearchResponseDto(2L, "test content2", "test title2", "test author2", "test publisher2", "test name 2")
                );

                when(reviewService.searchByKeyword(keyword)).thenReturn(mockResponse);

                // when
                ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.get(ReviewsApiUri.BASE)
                                .param("keyword", keyword)
                                .contentType(MediaType.APPLICATION_JSON)
                );

                // then

                resultActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.size()").value(2))
                        .andExpect(jsonPath("$[0].reviewId").value(1L))
                        .andExpect(jsonPath("$[0].content").value("test content1"))
                        .andExpect(jsonPath("$[1].reviewId").value(2L))
                        .andExpect(jsonPath("$[1].content").value("test content2"));

                verify(reviewService, times(1)).searchByKeyword(keyword);

            }
        }

        @Test
        @DisplayName("검색 실패 - 파라미터 없음")
        public void fail_no_param() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(ReviewsApiUri.BASE))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("검색 조건은 하나만 입력해야 합니다."));

			verifyNoInteractions(reviewService);

        }

        @Test
        @DisplayName("검색 실패 - 파라미터 2개 이상")
        void fail_multiple_params() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(ReviewsApiUri.BASE)
                            .param("title", "Java")
                            .param("author", "Smith"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("검색 조건은 하나만 입력해야 합니다."));

            verifyNoInteractions(reviewService);
        }

    }
}