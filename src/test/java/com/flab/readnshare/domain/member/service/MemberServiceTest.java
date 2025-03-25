package com.flab.readnshare.domain.member.service;

import com.flab.readnshare.domain.member.domain.EmailToken;
import com.flab.readnshare.domain.member.domain.Image;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.repository.EmailTokenRepository;
import com.flab.readnshare.domain.member.repository.ImageRepository;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.ImageException;
import com.flab.readnshare.global.common.exception.MemberException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private EmailTokenRepository emailTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("signUp 테스트")
    class signUp {
        @Test
        @DisplayName("성공")
        void success() throws MessagingException {
            // given
            SignUpRequestDto request = SignUpRequestDto.builder()
                    .email("test@naver.com")
                    .password("test24680!")
                    .nickName("testUser")
                    .build();

            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
            when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(Member.builder()
                    .email(request.getEmail())
                    .password("encodedPassword")
                    .nickName(request.getNickName())
                    .build());

            // EmailToken 생성 및 저장 시 어떤 토큰 값이든 받아들이도록 설정
            when(emailTokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
                EmailToken token = invocation.getArgument(0);
                // 저장된 토큰 객체를 그대로 반환
                return token;
            });

            // 토큰 캡처를 위한 ArgumentCaptor 생성
            ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);

            // when
            Member savedMember = memberService.signup(request);

            // then
            assertNotNull(savedMember);
            assertEquals("test@naver.com", savedMember.getEmail());
            assertEquals("encodedPassword", savedMember.getPassword());
            assertEquals("testUser", savedMember.getNickName());

            verify(memberRepository, times(1)).save(any(Member.class));

            // emailService.sendVerificationEmail 호출 시 전달된 실제 인자 캡처
            verify(emailService, times(1)).sendVerificationEmail(emailCaptor.capture(), tokenCaptor.capture());

            // 캡처된 값 검증
            assertEquals("test@naver.com", emailCaptor.getValue());
            // 토큰 값은 동적으로 생성되므로 null이 아닌지만 확인
            assertNotNull(tokenCaptor.getValue());
        }

        @Test
        @DisplayName("실패 - 중복된 이메일")
        void fail_duplicateEmail() {
            // given
            SignUpRequestDto request = SignUpRequestDto.builder()
                    .email("test@naver.com")
                    .password("test24680!")
                    .nickName("testUser")
                    .build();

            when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mock(Member.class)));

            // then
            assertThrows(MemberException.DuplicateEmailException.class,
                    () -> memberService.signup(request));
        }
    }

    @Nested
    @DisplayName("verifyEmail 테스트")
    class verifyEmail {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            String token = "valid-token";
            EmailToken emailToken = EmailToken.builder()
                    .email("test@naver.com")
                    .token(token)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .build();

            when(emailTokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));
            when(memberRepository.findByEmail(emailToken.getEmail())).thenReturn(Optional.of(Member.builder()
                    .email(emailToken.getEmail())
                    .build()));

            // when
            memberService.verifyEmail(token);

            // then
            verify(memberRepository, times(1)).save(any(Member.class));  // Member의 활성화 상태 변경 확인
            verify(emailTokenRepository, times(1)).delete(emailToken); // 이메일 토큰 삭제 확인
        }

        @Test
        @DisplayName("실패 - 만료된 토큰")
        void fail_expiredToken() {
            // given
            String token = "expired-token";
            EmailToken emailToken = EmailToken.builder()
                    .email("test@naver.com")
                    .token(token)
                    .expiryDate(LocalDateTime.now().minusHours(1))  // 이미 만료된 토큰
                    .build();

            when(emailTokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

            // then
            assertThrows(MemberException.ExpiredTokenException.class,
                    () -> memberService.verifyEmail(token));
        }

        @Test
        @DisplayName("실패 - 잘못된 토큰")
        void fail_invalidToken() {
            // given
            String token = "invalid-token";
            when(emailTokenRepository.findByToken(token)).thenReturn(Optional.empty());

            // then
            assertThrows(MemberException.InvalidTokenException.class,
                    () -> memberService.verifyEmail(token));
        }
    }

    @Nested
    @DisplayName("update 테스트")
    class update {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long memberId = 1L;
            Member existingMember = Member.builder()
                    .id(memberId)
                    .email("test@naver.com")
                    .password("oldPassword123!")
                    .nickName("oldNickName")
                    .profileContent("oldContent")
                    .profileImage(2L)
                    .build();

            UpdateRequestDto request = UpdateRequestDto.builder()
                    .password("newPassword24680!")
                    .nickName("newNickName")
                    .profileContent("newContent")
                    .profileImage(3L)
                    .build();

            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedNewPassword");
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

            // when
            Member updatedMember = memberService.update(memberId, request);

            // then
            assertNotNull(updatedMember);
            assertEquals("newNickName", updatedMember.getNickName());
            assertEquals("encodedNewPassword", updatedMember.getPassword());
            assertEquals("newContent", updatedMember.getProfileContent());
            assertEquals(3L, updatedMember.getProfileImage());
            assertEquals("test@naver.com", updatedMember.getEmail());

            verify(memberRepository, times(1)).save(existingMember);
        }
    }

    @Nested
    @DisplayName("findById 테스트")
    class findById {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long memberId = 1L;
            Member member = Member.builder()
                    .id(memberId)
                    .email("test@naver.com")
                    .nickName("testUser")
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

            // when
            Member foundMember = memberService.findById(memberId);

            // then
            assertNotNull(foundMember);
            assertEquals(memberId, foundMember.getId());
            assertEquals("test@naver.com", foundMember.getEmail());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원")
        void fail_memberNotFound() {
            // given
            Long memberId = 999L;
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // then
            assertThrows(MemberException.MemberNotFoundException.class, () -> memberService.findById(memberId));
        }
    }

    @Nested
    @DisplayName("findByImageId 테스트")
    class findByImageId {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            Long imageId = 1L;
            Image image = Image.builder()
                    .id(imageId)
                    .profileImagePath("https://image-url.com/image.png")
                    .build();

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

            // when
            Image foundImage = memberService.findByImageId(imageId);

            // then
            assertNotNull(foundImage);
            assertEquals(imageId, foundImage.getId());
            assertEquals("https://image-url.com/image.png", foundImage.getProfileImagePath());
        }

        @Test
        @DisplayName("실패 - 이미지 ID가 null")
        void fail_nullImageId() {
            // then
            assertThrows(IllegalArgumentException.class, () -> memberService.findByImageId(null));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 이미지 ID")
        void fail_imageNotFound() {
            // given
            Long imageId = 999L;
            when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

            // then
            assertThrows(ImageException.NotFoundImageException.class, () -> memberService.findByImageId(imageId));
        }
    }
}