package com.flab.readnshare.domain.auth.service;

import com.flab.readnshare.AuthTestFixture;
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

    @Test
    @DisplayName("로그인을 하면 member를 리턴한다")
    void signIn_success_return_member() {
        // given
        SignInRequestDto request = AuthTestFixture.getSignInRequestDto();

        Member expectedMember = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        expectedMember.setVerified(true);

        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(expectedMember));

        // when
        Member foundMember = authService.signIn(request);

        // then
        assertNotNull(foundMember);
        assertEquals(request.getEmail(), foundMember.getEmail());
    }

    @Test
    @DisplayName("비밀번호 불일치 시 InvalidEmailOrPasswordException 발생한다")
    void signIn_fail_password() {
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



    @Test
    @DisplayName("Refresh token이 Redis에 없는 경우 ExpiredTokenException이 발생한다")
    void refresh_fail_redis() {
        // given
        String refreshToken = "refreshTokenValue";
        when(refreshTokenRepository.findById(refreshToken)).thenReturn(Optional.empty());

        // when & then
        assertThrows(AuthException.ExpiredTokenException.class, () -> authService.validateTokenFromRedis(refreshToken));

    }

    @Nested
    @DisplayName("Refresh Token을 업데이트하면 새로운 Access Token과 Refresh Token이 발급된다.")
    class updateRefreshTokenTests {


        @Test
        @DisplayName("새로운 Access Token과 Refresh Token이 발급된다.")
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
