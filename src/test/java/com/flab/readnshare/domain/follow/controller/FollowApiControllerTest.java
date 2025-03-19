package com.flab.readnshare.domain.follow.controller;

import com.flab.readnshare.FollowTestFixture;
import com.flab.readnshare.domain.follow.facade.FollowFacade;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
import com.flab.readnshare.global.common.exception.FollowException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class FollowApiControllerTest {

    @Mock
    FollowFacade followFacade;

    @InjectMocks
    FollowApiController followApiController;

    @InjectMocks
    ApiExceptionAdvice apiExceptionAdvice;

    MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(followApiController)
                .setControllerAdvice(apiExceptionAdvice)
                .build();
    }

    @DisplayName("자기 자신을 팔로우하면 FollowException.SelfFollowException 예외가 발생한다.")
    @Test
    void followFailSelfFollow() throws Exception {
        // Given
        Member member = FollowTestFixture.getMemberEntity();
        String memberEmail = member.getEmail();
        given(followFacade.follow(anyString(), any(Member.class)))
                .willThrow(new FollowException.SelfFollowException());

        // When
        ResultActions resultActions = mockMvc.perform(
                post("/api/follow/{memberEmail}", memberEmail)
        );

        // Then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("자기 자신을 팔로우 할 수 없습니다."));
    }

    @DisplayName("이미 팔로우 한 사용자를 팔로우하면 FollowException.DuplicateFollowException 예외가 발생한다.")
    @Test
    void followFailDuplicateFollow() throws Exception {
        // Given
        Member member = FollowTestFixture.getMemberEntity();
        String memberEmail = member.getEmail();
        given(followFacade.follow(anyString(), any(Member.class)))
                .willThrow(new FollowException.DuplicateFollowException());

        // When
        ResultActions resultActions = mockMvc.perform(
                post("/api/follow/{memberEmail}", memberEmail)
        );

        // Then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 해당 사용자를 팔로우하고 있습니다."));
    }

    @DisplayName("특정 유저의 팔로워 목록을 조회한다.")
    @Test
    void followersOf() throws Exception {
        // Given
        List<MemberResponseDto> result = List.of();
        given(followFacade.getFollowersOf(anyString()))
                .willReturn(result);

        // When
        // Then
        mockMvc
                .perform(
                        get("/api/follow/followers/{memberEmail}", "test")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("특정 유저의 팔로잉 목록을 조회한다.")
    @Test
    void followingsOf() throws Exception {
        // Given
        List<MemberResponseDto> result = List.of();
        given(followFacade.getFollowingsOf(anyString()))
                .willReturn(result);

        // When
        // Then
        mockMvc
                .perform(
                        get("/api/follow/followings/{memberEmail}", "test")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}
