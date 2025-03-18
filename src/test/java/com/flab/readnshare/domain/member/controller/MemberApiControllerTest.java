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
    public void init(){
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
    @DisplayName("회원가입에 실패한다.(중복 이메일)")
    void signup_fail() throws Exception {
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
                .email("test@naver.com")
                .password("test24680!")
                .nickName("test")
                .build();

        when(memberService.signUp(any(SignUpRequestDto.class)))
                .thenThrow(new MemberException.DuplicateEmailException());

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/member/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입에 실패한다.(이메일 포맷)")
    void signup_fail_emali_format() throws Exception {
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
                .email("123")
                .password("test24680!")
                .nickName("test")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/member/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("이메일 형식이 맞지 않습니다."))
            ;
    }

    @Test
    @DisplayName("회원가입에 실패한다.(비밀번호 포맷)")
    void signup_fail_password_format() throws Exception {
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
                .email("test@naver.com")
                .password("123")
                .nickName("test")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/member/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요."))
        ;
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
                MockMvcRequestBuilders.put("/api/member/1") // PUT 메서드로 변경
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@naver.com")) // 기존 이메일
                .andExpect(jsonPath("$.nickName").value(request.getNickName())); // 변경된 닉네임

    }

    @Test
    @DisplayName("회원수정에 실패한다.(비밀번호)")
    void update_fail_password_format() throws Exception {
        // given
        UpdateRequestDto request = UpdateRequestDto.builder()
                .password("123")
                .nickName("test")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/member/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요."))
        ;
    }

    @Test
    @DisplayName("회원수정에 실패한다.(닉네임 없음)")
    void update_fail_nickName_format() throws Exception {
        // given
        UpdateRequestDto request = UpdateRequestDto.builder()
                .password("test24680!")
                .nickName("")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/member/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값을 확인하세요."))
                .andExpect(jsonPath("$.errors[0].field").value("nickName"))
                .andExpect(jsonPath("$.errors[0].message").value("닉네임을 입력해주세요."))
        ;
    }

    @DisplayName("존재하지 않는 회원을 삭제하면 404를 반환한다.")
    void delete_fail_member_not_found() throws Exception {
        // given
        Long memberId = 1L;

        // memberService.findById()가 null을 반환하도록 설정
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