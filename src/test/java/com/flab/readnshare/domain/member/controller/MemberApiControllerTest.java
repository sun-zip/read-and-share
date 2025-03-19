package com.flab.readnshare.domain.member.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.service.MemberService;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
import com.flab.readnshare.global.common.exception.MemberException;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MemberApiControllerTest {
    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberApiController memberApiController;

    @InjectMocks
    ApiExceptionAdvice apiExceptionAdvice;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(memberApiController)
                .setControllerAdvice(apiExceptionAdvice)
                .build();
    }

    @Nested
    @DisplayName("signUp 테스트")
    class signup {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            SignUpRequestDto request = SignUpRequestDto.builder()
                    .email("test@naver.com")
                    .password("test24680!")
                    .nickName("test")
                    .build();

            given(memberService.signUp(any(SignUpRequestDto.class), any()))
                    .willReturn(MemberResponseDto.builder()
                            .id(1L)
                            .email("test@naver.com")
                            .nickName("test")
                            .build().toEntity());

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/member/signUp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // then
            resultActions.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(request.getEmail()))
                    .andExpect(jsonPath("$.nickName").value(request.getNickName()));
        }
    }

    @Nested
    @DisplayName("update 테스트")
    class update {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            UpdateRequestDto request = UpdateRequestDto.builder()
                    .password("test24680!")
                    .nickName("test")
                    .build();

            when(memberService.update(anyLong(), any(UpdateRequestDto.class), any()))
                    .thenReturn(MemberResponseDto.builder()
                            .id(1L)
                            .email("test@naver.com")
                            .nickName("test")
                            .build().toEntity());


            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.put("/api/member/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(request))
            );

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@naver.com"))
                    .andExpect(jsonPath("$.nickName").value("test"));
        }
    }

    @Nested
    @DisplayName("searchMember 테스트")
    class searchMember {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            String email = "test@naver.com";
            Member member = Member.builder()
                    .id(1L)
                    .email(email)
                    .nickName("testUser")
                    .build();

            when(memberService.findByEmail(email)).thenReturn(member);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/member/search")
                            .param("email", email)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.nickName").value("testUser"));
        }

        @Test
        @DisplayName("실패 - 회원이 없는 경우")
        void fail_memberNotFound() throws Exception {
            // given
            String email = "notfound@naver.com";

            when(memberService.findByEmail(email)).thenThrow(new MemberException.MemberNotFoundException());

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/member/search")
                            .param("email", email)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
        }
    }

    @Nested
    @DisplayName("delete 테스트")
    class delete {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            Long memberId = 1L;

            Member member = Member.builder()
                    .id(memberId)
                    .email("test@naver.com")
                    .password("password123!")
                    .nickName("testUser")
                    .build();

            when(memberService.findById(memberId)).thenReturn(member);
            doNothing().when(memberService).delete(memberId);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.delete("/api/member/{memberId}", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 회원이 없는 경우")
        void fail_memberNotFound() throws Exception {
            // given
            Long memberId = 1L;

            when(memberService.findById(memberId)).thenReturn(null);

            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.delete("/api/member/{memberId}", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isNotFound());
        }
    }
}
