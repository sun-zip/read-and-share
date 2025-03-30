package com.flab.readnshare.domain.auth.service;

import com.flab.readnshare.domain.auth.domain.RefreshToken;
import com.flab.readnshare.domain.auth.dto.SignInRequestDto;
import com.flab.readnshare.domain.auth.repository.RefreshTokenRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.auth.jwt.JwtUtil;
import com.flab.readnshare.global.common.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인, 토큰 발급, 토큰 검증 등의 인증 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // 이메일로 회원을 조회하고 비밀번호를 검증하여 로그인을 처리하는 메서드
    public Member signIn(SignInRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(AuthException.InvalidEmailOrPasswordException::new);
        validatePassword(dto.getPassword(), member.getPassword());
        if(!member.isVerified()) {
            throw new AuthException.UnverifiedEmailException();
        }
        return member;
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new AuthException.InvalidEmailOrPasswordException();
        }
    }

    // memberId에 대한 access 토큰을 생성 후 HTTP 응답 헤더에 설정
    public void sendAccessToken(HttpServletResponse response, Long memberId) {
        String accessToken = jwtUtil.createAccessToken(memberId);
        jwtUtil.setAccessTokenHeader(response, accessToken);
    }

    // memberId에 대한 refresh 토큰을 생성 후 HTTP 응답 쿠키에 설정
    public void sendRefreshToken(HttpServletRequest request, HttpServletResponse response, Long memberId) {
        String clientIp = request.getRemoteAddr();
        String refreshToken = jwtUtil.createRefreshToken(memberId, clientIp);
        jwtUtil.setRefreshTokenCookie(response, refreshToken);
    }
    // memberId에 대한 access, refresh 토큰을 생성 후 HTTP 응답 헤더와 쿠키에 설정
    public void issueTokens(HttpServletRequest request, HttpServletResponse response, Long memberId) {
        sendAccessToken(response, memberId);
        sendRefreshToken(request, response, memberId);
        log.info("AccessToken, RefreshToken 발급 완료 - memberId: {}", memberId);
    }

    // refresh 토큰을 삭제하고 새로운 access, refresh 토큰을 발급
    //토큰 재발급 시 클라이언트 IP가 저장된 IP와 일치하는지 검증
    public void updateRefreshToken(HttpServletRequest request, HttpServletResponse response, Long memberId, String oldRefreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findById(oldRefreshToken)
                .orElseThrow(AuthException.ExpiredTokenException::new);
        String requestIp = request.getRemoteAddr();

        if (!storedToken.getIpAddress().equals(requestIp)) {
            throw new AuthException.DeniedTokenException();
        }

        refreshTokenRepository.deleteById(oldRefreshToken);
        issueTokens(request, response, memberId);
    }

    // refresh 토큰을 검증
    public void validateTokenFromRedis(String refreshToken) {
        if (refreshTokenRepository.findById(refreshToken).isEmpty()) {
            throw new AuthException.ExpiredTokenException();
        }
    }
}
