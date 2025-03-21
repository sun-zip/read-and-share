package com.flab.readnshare.domain.feed.controller;

import com.flab.readnshare.domain.feed.dto.FeedRequestDto;
import com.flab.readnshare.domain.feed.dto.FeedResponseDto;
import com.flab.readnshare.domain.feed.facade.FeedFacade;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedApiControllerTest {

    static final String GET_FEEDS_API_ENDPOINT = "/api/v1/feeds";

    @Mock
    FeedFacade feedFacade;

    @InjectMocks
    FeedApiController feedApiController;

    MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(feedApiController)
                .build();
    }

    @Nested
    @DisplayName("getFeeds 테스트")
    class getFeedsTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            FeedResponseDto response = FeedResponseDto.builder()
                    .reviewId(1L)
                    .nickName("test")
                    .content("test")
                    .bookTitle("test")
                    .build();

            BDDMockito.given(feedFacade.getFeeds(any(), anyLong(), anyInt()))
                    .willReturn(List.of(response));
            // When
            ResultActions resultActions = mockMvc.perform(
                    get(GET_FEEDS_API_ENDPOINT)
                            .param("lastReviewId", "1")
                            .param("limit", "1")
            );

            // Then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].reviewId").value(1L))
                    .andExpect(jsonPath("$.[0].nickName").value("test"))
                    .andExpect(jsonPath("$.[0].content").value("test"))
                    .andExpect(jsonPath("$.[0].bookTitle").value("test"));
        }

        @Test
        @DisplayName("실패 - lastReivewId 음수")
        void fail_negative_last_review_id() throws Exception {
            // When
            ResultActions resultActions = mockMvc.perform(
                    get(GET_FEEDS_API_ENDPOINT)
                            .param("lastReviewId", "-1")
            );

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - limit 음수")
        void fail_negative_limit() throws Exception {
            // When
            ResultActions resultActions = mockMvc.perform(
                    get(GET_FEEDS_API_ENDPOINT)
                            .param("limit", "-1")
            );

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - limit 10보다 큼")
        void fail_limit_greater_than_10() throws Exception {
            // When
            ResultActions resultActions = mockMvc.perform(
                    get(GET_FEEDS_API_ENDPOINT)
                            .param("limit", "11")
            );

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

}
