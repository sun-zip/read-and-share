package com.flab.readnshare.domain.auth.service;

import com.flab.readnshare.AuthTestFixture;
import com.flab.readnshare.domain.auth.domain.RefreshToken;
import com.flab.readnshare.domain.auth.dto.SignInRequestDto;
import com.flab.readnshare.domain.auth.repository.RefreshTokenRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.auth.jwt.JwtUtil;
import com.flab.readnshare.global.common.exception.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    //private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("signIn 테스트")
    class SignInTests {
        @Test
        @DisplayName("성공 - member를 리턴")
        void signIn_success_return_member() {
            // given
            SignInRequestDto request = AuthTestFixture.getSignInRequestDto();

            Member expectedMember = Member.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            expectedMember.setVerified(true);

            when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(expectedMember));
            //when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
            // when
            Member foundMember = authService.signIn(request);

            // then
            assertNotNull(foundMember);
            assertEquals(request.getEmail(), foundMember.getEmail());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void signIn_fail_unverified_email_exception() {
            // given
            SignInRequestDto request = AuthTestFixture.getSignInRequestDto();

            Member expectedMember = Member.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            expectedMember.setVerified(false);

            when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(expectedMember));

            // when
            assertThrows(AuthException.UnverifiedEmailException.class, () -> authService.signIn(request));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void signIn_fail_invalid_email_or_password_exception() {
            // given
            SignInRequestDto request = AuthTestFixture.getSignInRequestDto();

            Member expectedMember = Member.builder()
                    .email(request.getEmail())
                    .password("test12345!")
                    .build();

            when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(expectedMember));

            // when & then
            assertThrows(AuthException.InvalidEmailOrPasswordException.class, () -> authService.signIn(request));
        }
    }

    @Nested
    @DisplayName("validateTokenFromRedis 테스트")
    class ValidateTokenFromRedisTests {
        @Test
        @DisplayName("성공 - Refresh token이 Redis에 있음")
        void success() {
            String refreshTokenStr = "refreshTokenValue";
            RefreshToken refreshToken = RefreshToken.builder()
                    .refreshTokenValue(refreshTokenStr)
                    .memberId(1L)
                    .expiration(1000L)
                    .build();

            when(refreshTokenRepository.findById(refreshTokenStr)).thenReturn(Optional.of(refreshToken));

            authService.validateTokenFromRedis(refreshTokenStr);

            verify(refreshTokenRepository).findById(refreshTokenStr);

        }


        @Test
        @DisplayName("실패 - Refresh token이 Redis에 없음")
        void refresh_fail_expired_token_exception() {
            // given
            String refreshToken = "refreshTokenValue";
            when(refreshTokenRepository.findById(refreshToken)).thenReturn(Optional.empty());

            // when & then
            assertThrows(AuthException.ExpiredTokenException.class, () -> authService.validateTokenFromRedis(refreshToken));

        }
    }


    @Nested
    @DisplayName("updateRefreshToken 테스트")
    class updateRefreshTokenTests {
        @Test
        @DisplayName("성공 - 새로운 Access Token과 Refresh Token이 발급된다.")
        void updateRefreshToken_success() {
            // given
            String oldRefreshToken = "oldTokenValue";
            Long memberId = 1L;
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

            String newAccessToken = "newAccessToken";
            String newRefreshToken = "newRefreshToken";

            when(jwtUtil.createAccessToken(memberId)).thenReturn(newAccessToken);
            when(jwtUtil.createRefreshToken(memberId)).thenReturn(newRefreshToken);

            // when
            authService.updateRefreshToken(response, memberId, oldRefreshToken);

            // then
            verify(refreshTokenRepository).deleteById(oldRefreshToken);
            verify(jwtUtil).createAccessToken(memberId);
            verify(jwtUtil).createRefreshToken(memberId);
            verify(jwtUtil).setAccessTokenHeader(response, newAccessToken);
            verify(jwtUtil).setRefreshTokenCookie(response, newRefreshToken);


        }
    }
}
