package com.flab.readnshare.domain.follow.controller;

import com.flab.readnshare.FollowTestFixture;
import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.follow.facade.FollowFacade;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
import com.flab.readnshare.global.common.exception.FollowException;
import com.flab.readnshare.global.common.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FollowApiControllerTest {

    static final String FOLLOW_API_ENDPOINT = "/api/follow/{memberEmail}";
    static final String UNFOLLOW_API_ENDPOINT = "/api/follow/{memberEmail}";
    static final String FOLLOWERS_API_ENDPOINT = "/api/follow/followers/{memberEmail}";
    static final String FOLLOWINGS_API_ENDPOINT = "/api/follow/followings/{memberEmail}";

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

    @Nested
    @DisplayName("follow 테스트")
    class followTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member fromMember = FollowTestFixture.getMemberEntity();
            Member toMember = FollowTestFixture.getMemberEntity();
            String memberEmail = toMember.getEmail();
            Follow follow = FollowTestFixture.getFollowEntity(fromMember, toMember);

            given(followFacade.follow(anyString(), any(Member.class)))
                    .willReturn(follow);

            // When
            ResultActions resultActions = mockMvc.perform(
                    post(FOLLOW_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저를 팔로우")
        void fail_not_found_member() throws Exception {
            // Given
            Member fromMember = FollowTestFixture.getMemberEntity();
            Member toMember = FollowTestFixture.getMemberEntity();
            String memberEmail = toMember.getEmail();

            willThrow(new MemberException.MemberNotFoundException())
                    .given(followFacade).follow(anyString(), any(Member.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    post(FOLLOW_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions.andExpect(status().isNotFound())
                    .andDo(print())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
        }

        @Test
        @DisplayName("실패 - 자기 자신 팔로우")
        void fail_self_follow() throws Exception {
            // Given
            Member member = FollowTestFixture.getMemberEntity();
            String memberEmail = member.getEmail();
            given(followFacade.follow(anyString(), any(Member.class)))
                    .willThrow(new FollowException.SelfFollowException());

            // When
            ResultActions resultActions = mockMvc.perform(
                    post(FOLLOW_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions.andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message").value("자기 자신을 팔로우 할 수 없습니다."));
        }

        @Test
        @DisplayName("실패 - 중복 팔로우")
        void fail_duplicate_follow() throws Exception {
            // Given
            Member member = FollowTestFixture.getMemberEntity();
            String memberEmail = member.getEmail();
            given(followFacade.follow(anyString(), any(Member.class)))
                    .willThrow(new FollowException.DuplicateFollowException());

            // When
            ResultActions resultActions = mockMvc.perform(
                    post(FOLLOW_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions.andExpect(status().isBadRequest())
                    .andDo(print())
                    .andExpect(jsonPath("$.message").value("이미 해당 사용자를 팔로우하고 있습니다."));
        }
    }

    @Nested
    @DisplayName("unfollow 테스트")
    class unfollowTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member fromMember = FollowTestFixture.getMemberEntity();
            Member toMember = FollowTestFixture.getMemberEntity();
            String memberEmail = toMember.getEmail();

            willDoNothing()
                    .given(followFacade).unfollow(anyString(), any(Member.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete(UNFOLLOW_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저를 언팔로우")
        void fail_not_found_member() throws Exception {
            // Given
            Member fromMember = FollowTestFixture.getMemberEntity();
            Member toMember = FollowTestFixture.getMemberEntity();
            String memberEmail = toMember.getEmail();

            willThrow(new MemberException.MemberNotFoundException())
                    .given(followFacade).unfollow(anyString(), any(Member.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete(UNFOLLOW_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions.andExpect(status().isNotFound())
                    .andDo(print())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
        }
    }

    @Nested
    @DisplayName("followersOf 테스트")
    class followersOfTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            List<MemberResponseDto> result = List.of();
            given(followFacade.getFollowersOf(anyString()))
                    .willReturn(result);

            // When
            ResultActions resultActions = mockMvc.perform(
                    get(FOLLOWERS_API_ENDPOINT, "test")
            );

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저의 팔로우 조회")
        void fail_not_found_member() throws Exception {
            // Given
            Member toMember = FollowTestFixture.getMemberEntity();
            String memberEmail = toMember.getEmail();

            willThrow(new MemberException.MemberNotFoundException())
                    .given(followFacade).getFollowersOf(anyString());

            // When
            ResultActions resultActions = mockMvc.perform(
                    get(FOLLOWERS_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions.andExpect(status().isNotFound())
                    .andDo(print())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
        }
    }

    @Nested
    @DisplayName("followingsOf 테스트")
    class followingsOfTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            List<MemberResponseDto> result = List.of();
            given(followFacade.getFollowingsOf(anyString()))
                    .willReturn(result);

            // When
            ResultActions resultActions = mockMvc.perform(
                    get(FOLLOWINGS_API_ENDPOINT, "test")
            );

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저의 팔로잉 조회")
        void fail_not_found_member() throws Exception {
            // Given
            Member toMember = FollowTestFixture.getMemberEntity();
            String memberEmail = toMember.getEmail();

            willThrow(new MemberException.MemberNotFoundException())
                    .given(followFacade).getFollowingsOf(anyString());

            // When
            ResultActions resultActions = mockMvc.perform(
                    get(FOLLOWINGS_API_ENDPOINT, memberEmail)
            );

            // Then
            resultActions.andExpect(status().isNotFound())
                    .andDo(print())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
        }
    }
}
