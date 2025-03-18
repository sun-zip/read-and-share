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

    @Test
    @DisplayName("회원가입에 성공한다.")
    void signup_success() throws Exception {
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
                .email("test@naver.com")
                .password("test24680!")
                .nickName("test")
                .build();

        given(memberService.signUp(any(SignUpRequestDto.class)))
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

    @Test
    @DisplayName("회원수정에 성공한다.")
    void update_success() throws Exception {
        // given
        UpdateRequestDto request = UpdateRequestDto.builder()
                .password("test24680!")
                .nickName("test")
                .build();

        given(memberService.update(any(Long.class), any(UpdateRequestDto.class)))
                .willReturn(MemberResponseDto.builder()
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

    @Test
    @DisplayName("이메일로 회원 검색 성공")
    void searchMember_Success() throws Exception {
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
    @DisplayName("이메일로 회원 검색 실패 - 존재하지 않는 회원")
    void searchMember_Fail_NotFound() throws Exception {
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

    @Test
    @DisplayName("존재하지 않는 회원을 삭제하면 404를 반환한다.")
    void delete_fail_member_not_found() throws Exception {
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

    @Test
    @DisplayName("존재하는 회원을 삭제하면 200을 반환한다.")
    void delete_success_member() throws Exception {
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
}
