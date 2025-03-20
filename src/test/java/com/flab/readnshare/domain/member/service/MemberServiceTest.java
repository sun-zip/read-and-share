package com.flab.readnshare.domain.member.service;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;  // Ensure this is mocked

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("signUp 테스트")
    class signUp {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            SignUpRequestDto request = SignUpRequestDto.builder()
                    .email("test@naver.com")
                    .password("test24680!")
                    .nickName("test")
                    .build();

            // 암호화된 비밀번호를 가상으로 설정
            String encodedPassword = "encodedNewPassword"; // 가상의 암호화된 비밀번호
            when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

            // 회원이 존재하지 않으므로 Optional.empty()를 반환
            when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

            // expectedMember 생성 시, 암호화된 비밀번호를 사용하여 비교
            Member expectedMember = Member.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)  // 암호화된 비밀번호 사용
                    .nickName(request.getNickName())
                    .build();

            // save()가 호출되면 expectedMember를 반환하도록 설정
            when(memberRepository.save(any(Member.class))).thenReturn(expectedMember);

            // when
            // SignUpRequestDto의 toEntity 호출 시 passwordEncoder를 전달
            Member savedMember = memberService.signUp(request, passwordEncoder);

            // then
            assertNotNull(savedMember);
            assertEquals("test@naver.com", savedMember.getEmail());
            assertEquals(encodedPassword, savedMember.getPassword());  // 암호화된 비밀번호가 맞는지 확인
            assertEquals("test", savedMember.getNickName());

            // memberRepository의 save()가 한 번 호출되었는지 검증
            verify(memberRepository, times(1)).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 중복된 이메일")
        void fail_duplicateEmail() {
            // given
            SignUpRequestDto request = SignUpRequestDto.builder()
                    .email("test@naver.com")
                    .password("test24680!")
                    .nickName("test")
                    .build();

            Optional<Member> m = Optional.of(mock(Member.class));
            when(memberRepository.findByEmail(request.getEmail())).thenReturn(m);

            // then
            assertThrows(MemberException.DuplicateEmailException.class, () -> memberService.signUp(request, passwordEncoder), "이메일이 중복되어 회원가입에 실패해야 합니다.");
        }
    }

    @Nested
    @DisplayName("update 테스트")
    class update {
        @Test
        @DisplayName("성공")
        void success() {
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

            // toEntity()로 비밀번호 암호화된 업데이트된 멤버 생성
            String encodedPassword = "encodedNewPassword"; // 가상의 암호화된 비밀번호
            when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

            // 기존 회원이 존재하는 경우를 가정
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

            // when (실제 메소드를 호출하여 회원정보 수정)
            Member updatedMember = memberService.update(memberId, request, passwordEncoder);

            // then (업데이트된 멤버의 닉네임과 비밀번호가 올바르게 수정되었는지 확인)
            assertNotNull(updatedMember);
            assertEquals("newNickName", updatedMember.getNickName());
            assertEquals(encodedPassword, updatedMember.getPassword());
            assertEquals("test@naver.com", updatedMember.getEmail()); // 이메일은 수정되지 않아야 함

            // memberRepository의 save()가 호출되었는지 검증
            verify(memberRepository, times(1)).save(updatedMember);
        }
    }
}
