package com.flab.readnshare.domain.member.service;

import com.flab.readnshare.domain.member.domain.Image;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.repository.ImageRepository;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.ImageException;
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
    private ImageRepository imageRepository;  // 추가된 ImageRepository 목 객체

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            SignUpRequestDto request = SignUpRequestDto.builder()
                    .email("test@naver.com")
                    .password("test24680!")
                    .nickName("testUser")
                    .build();

            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
            when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            Member expectedMember = Member.builder()
                    .email(request.getEmail())
                    .password("encodedPassword")
                    .nickName(request.getNickName())
                    .build();

            when(memberRepository.save(any(Member.class))).thenReturn(expectedMember);

            // when
            Member savedMember = memberService.signUp(request, passwordEncoder);

            // then
            assertNotNull(savedMember);
            assertEquals("test@naver.com", savedMember.getEmail());
            assertEquals("encodedPassword", savedMember.getPassword());
            assertEquals("testUser", savedMember.getNickName());
            verify(memberRepository, times(1)).save(any(Member.class));
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
                    () -> memberService.signUp(request, passwordEncoder));
        }
    }

    @Nested
    @DisplayName("회원 정보 수정 테스트")
    class UpdateTest {
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
            Member updatedMember = memberService.update(memberId, request, passwordEncoder);

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
    @DisplayName("회원 조회 테스트")
    class FindByIdTest {
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
    @DisplayName("이미지 조회 테스트")
    class FindByImageIdTest {
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
