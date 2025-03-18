package com.flab.readnshare.domain.review.controller;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.SaveReviewRequestDto;
import com.flab.readnshare.domain.review.facade.ReviewFacade;
import com.flab.readnshare.domain.review.service.ReviewService;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
import com.flab.readnshare.global.common.exception.ReviewException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
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

    @Test
    @DisplayName("독서 기록 등록에 성공한다.")
    void save_success() throws Exception {
        // given
        SaveReviewRequestDto request = ReviewTestFixture.getSaveReviewRequestDto();

        given(reviewFacade.save(any(SaveReviewRequestDto.class), any(Member.class))).willReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("독서 기록 등록에 실패한다. (내용 없음)")
    void save_fail_no_content() throws Exception {
        SaveReviewRequestDto request = mock(SaveReviewRequestDto.class);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/review")
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
    void save_fail_no_isbn() throws Exception {
        SaveReviewRequestDto request = SaveReviewRequestDto.builder()
                .content("내용")
                .book(BookDto.builder().id(1L).isbn("").title("테스트").build())
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/review")
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
    void save_fail_no_title() throws Exception {
        SaveReviewRequestDto request = SaveReviewRequestDto.builder()
                .content("내용")
                .book(BookDto.builder().id(1L).isbn("1234").title("").build())
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                .andExpect(jsonPath("$.errors[0].field").value("book.title"))
                .andExpect(jsonPath("$.errors[0].message").value("책 제목이 없습니다."));
    }

    @DisplayName("독서 기록 삭제에 성공한다.")
    @Test
    void delete_review_success() throws Exception {
        // Given
        Review review = ReviewTestFixture.getReviewEntity();
        Long reviewId = review.getId();
        doNothing().when(reviewFacade).delete(anyLong(), any(Member.class));

        // When
        ResultActions resultActions = mockMvc.perform(
                delete("/api/review/{reviewId}", reviewId)
        );

        // Then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("존재하지 않는 리뷰를 삭제하면 REVIEW_NOT_FOUND 예외가 발생한다.")
    @Test
    void delete_review_fail_not_found() throws Exception {
        // Given
        Review review = ReviewTestFixture.getReviewEntity();
        Long reviewId = review.getId();
        doThrow(new ReviewException.ReviewNotFoundException()).when(reviewFacade).delete(anyLong(), any(Member.class));

        // When
        ResultActions resultActions = mockMvc.perform(
                delete("/api/review/{reviewId}", reviewId)
        );

        // Then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 독서 기록 입니다."));
    }

    @DisplayName("삭제 권한이 없는 사용자가 리뷰를 삭제할 경우 REVIEW_FORBIDDEN_MEMBER 에러가 발생한다.")
    @Test
    void delete_review_fail_no_permission() throws Exception {
        // Given
        Review review = ReviewTestFixture.getReviewEntity();
        Long reviewId = review.getId();
        doThrow(new ReviewException.ForbiddenMemberException()).when(reviewFacade).delete(anyLong(), any(Member.class));

        // When
        ResultActions resultActions = mockMvc.perform(
                delete("/api/review/{reviewId}", reviewId)
        );

        // Then
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 독서 기록에 대한 수정 및 삭제 권한이 없습니다."));
    }

}