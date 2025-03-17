package com.flab.readnshare.domain.member.service;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원가입을 하면 member가 등록된다")
    void signup_success_add_member(){
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
                .email("test@naver.com")
                .password("test24680!")
                .nickName("test")
                .build();

        Member expectedMember = Member.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .nickName(request.getNickName())
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(expectedMember);
        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        // when
        Member savedMember = memberService.signUp(request);

        // then
        assertNotNull(savedMember);
        assertEquals("test@naver.com", savedMember.getEmail());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 DuplicateEmailException이 발생한다")
    void signup_fail_duplicate_email(){
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
                .email("test@naver.com")
                .password("test24680!")
                .nickName("test")
                .build();

        Optional<Member> m = Optional.of(mock(Member.class));
        when(memberRepository.findByEmail(request.getEmail())).thenReturn(m);

        // then
        assertThrows(MemberException.DuplicateEmailException.class, () -> memberService.signUp(request),"이메일이 중복되어 회원가입에 실패해야 합니다.");
    }

    @Test
    @DisplayName("회원 정보를 수정하면 성공적으로 업데이트된다.")
    void update_success_member() {
        // given (기존 회원 정보)
        Long memberId = 1L;
        Member existingMember = Member.builder()
                .id(memberId)
                .email("test@naver.com")  // 기본 이메일은 수정하지 않음
                .password("oldPassword123!")
                .nickName("oldNickName")
                .build();

        UpdateRequestDto request = UpdateRequestDto.builder()
                .password("newPassword24680!") // 새로운 비밀번호
                .nickName("newNickName")       // 새로운 닉네임
                .build();

        // 기존 회원이 존재하는 경우를 가정
        // findById()로 given으로 주어진 existingMember를 반환
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

        // update()는 void이므로 doNothing() 사용
        // update()를 사용하였을 때 예외처리 안하도록 설정하는 코드
        doNothing().when(memberRepository).update(memberId, request.getNickName(), request.getPassword());

        // when (실제 메소드를 불러와 회원정보 수정)
        memberService.update(memberId, request);

        // then (update가 딱 한번 잘 호출했는지 확인: times)
        verify(memberRepository, times(1)).update(memberId, request.getNickName(), request.getPassword());
    }
}