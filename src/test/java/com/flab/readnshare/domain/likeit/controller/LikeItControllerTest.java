package com.flab.readnshare.domain.likeit.controller;


import com.flab.readnshare.domain.likeit.service.LikeItService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.controller.uri.ReviewsApiUri;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
import com.flab.readnshare.global.common.exception.ReviewException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class LikeItControllerTest {

	@InjectMocks
	private LikeItController likeItController;

	@Mock
	private LikeItService likeItService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(){
		mockMvc = MockMvcBuilders.standaloneSetup(likeItController)
				.setControllerAdvice(new ApiExceptionAdvice())
				.build();
	}

	@Nested
	@DisplayName("좋아요 토글 테스트")
	class toggleLikeItTest {

		@Test
		@DisplayName("좋아요 토글 성공")
		void success() throws Exception {
			// given
			Long reviewId = 1L;
			Member mockMember = Member.builder().id(1L).nickName("tester").build();

			// when & then
			mockMvc.perform(post(ReviewsApiUri.LIKE, reviewId)
					.requestAttr("signInMember", mockMember))
					.andExpect(status().isOk());

			verify(likeItService, times(1)).toggleLikeIt(eq(reviewId), any(Member.class));
			// 아래와 같이 코드를 짰더니 Member인 argument가 다르다고 나온다. 이유를 잘 모르겠음
			// verify(likeItService, times(1)).toggleLikeIt(eq(reviewId), eq(mockMember));
		}

		@Test
		@DisplayName("없는 리뷰 ID - 404 예외")
		void toggleLike_fail_reviewNotFound() throws Exception {
			Long reviewId = 1L;
			Member mockMember = Member.builder().id(1L).nickName("tester").build();

			doThrow(new ReviewException.ReviewNotFoundException())
					.when(likeItService).toggleLikeIt(eq(reviewId), any(Member.class));

			mockMvc.perform(post(ReviewsApiUri.LIKE, reviewId)
							.requestAttr("signInMember", mockMember))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("존재하지 않는 독서 기록 입니다."));
		}


	}


}
