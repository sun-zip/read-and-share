package com.flab.readnshare.domain.notification.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.notification.dto.SaveFCMTokenRequestDto;
import com.flab.readnshare.domain.notification.service.FCMService;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FCMApiControllerTest {

    static final String SAVE_FCMTOKEN_API_ENDPOINT = "/api/v1/fcms";

    @Mock
    FCMService fcmService;

    @InjectMocks
    FCMApiController fcmApiController;

    @InjectMocks
    ApiExceptionAdvice apiExceptionAdvice;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(fcmApiController)
                .setControllerAdvice(apiExceptionAdvice)
                .build();
    }

    @Nested
    @DisplayName("saveFCMToken 테스트")
    class saveFCMTokenTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            SaveFCMTokenRequestDto request = SaveFCMTokenRequestDto.builder()
                    .token("test")
                    .build();

            willDoNothing()
                    .given(fcmService).saveFCMToken(any(Member.class), anyString());

            // When
            ResultActions resultActions = mockMvc.perform(
                    post(SAVE_FCMTOKEN_API_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // Then
            resultActions.andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 토큰 공백")
        void fail_token_blank() throws Exception {
            // Given
            SaveFCMTokenRequestDto request = SaveFCMTokenRequestDto.builder()
                    .token("    ")
                    .build();

            ResultActions resultActions = mockMvc.perform(
                    post(SAVE_FCMTOKEN_API_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // When

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                    .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));
        }

        @Test
        @DisplayName("실패 - RequestBody 누락")
        void fail_no_body() throws Exception {
            // Given
            ResultActions resultActions = mockMvc.perform(
                    post(SAVE_FCMTOKEN_API_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // When

            // Then
            resultActions.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("입력값을 확인하세요."));
        }
    }
}