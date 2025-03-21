package com.flab.readnshare.domain.notification.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.notification.dto.SaveFCMTokenRequestDto;
import com.flab.readnshare.domain.notification.service.FCMService;
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

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FCMApiControllerTest {

    static final String SAVE_FCMTOKEN_API_ENDPOINT = "/api/v1/fcms";

    @Mock
    FCMService fcmService;

    @InjectMocks
    FCMApiController fcmApiController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(fcmApiController)
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
                    MockMvcRequestBuilders.post(SAVE_FCMTOKEN_API_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // Then
            resultActions.andExpect(status().isOk());
        }
    }
}