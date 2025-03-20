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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedApiControllerTest {

    static final String GET_FEED = "/api/v1/feeds";

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
    @DisplayName("getFeed 테스트")
    class getFeedTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            FeedRequestDto request = FeedRequestDto.builder()
                    .lastReviewId(1L)
                    .limit(1)
                    .build();

            FeedResponseDto response = FeedResponseDto.builder()
                    .reviewId(1L)
                    .nickName("test")
                    .content("test")
                    .bookTitle("test")
                    .build();

            BDDMockito.given(feedFacade.getFeed(any(), anyLong(), anyInt()))
                    .willReturn(List.of(response));
            // When
            ResultActions resultActions = mockMvc.perform(
                    get(GET_FEED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // Then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].reviewId").value(1L))
                    .andExpect(jsonPath("$.[0].nickName").value("test"))
                    .andExpect(jsonPath("$.[0].content").value("test"))
                    .andExpect(jsonPath("$.[0].bookTitle").value("test"));
        }
    }

}