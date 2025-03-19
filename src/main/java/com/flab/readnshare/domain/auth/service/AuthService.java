package com.flab.readnshare.domain.auth.service;

import com.flab.readnshare.domain.auth.dto.SignInRequestDto;
import com.flab.readnshare.domain.auth.repository.RefreshTokenRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.auth.jwt.JwtUtil;
import com.flab.readnshare.global.common.exception.AuthException;
import com.flab.readnshare.global.common.exception.MemberException;
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
        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())){
            throw new AuthException.InvalidEmailOrPasswordException();
        }
        return member;
    }

    // memberId에 대한 access 토큰을 생성 후 HTTP 응답 헤더에 설정
    public void sendAccessToken(HttpServletResponse response, Long memberId) {
        String accessToken = jwtUtil.createAccessToken(memberId);
        jwtUtil.setAccessTokenHeader(response, accessToken);
    }

    // memberId에 대한 refresh 토큰을 생성 후 HTTP 응답 쿠키에 설정
    public void sendRefreshToken(HttpServletResponse response, Long memberId) {
        String refreshToken = jwtUtil.createRefreshToken(memberId);
        jwtUtil.setRefreshTokenCookie(response, refreshToken);
    }

    // refresh 토큰을 삭제하고 새로운 access, refresh 토큰을 발급
    public void updateRefreshToken(HttpServletResponse response, Long memberId, String oldRefreshToken) {
        refreshTokenRepository.deleteById(oldRefreshToken);
        sendAccessToken(response, memberId);
        sendRefreshToken(response, memberId);
    }

    // refresh 토큰을 검증
    public void validateTokenFromRedis(String refreshToken) {
        if (refreshTokenRepository.findById(refreshToken).isEmpty()) {
            throw new AuthException.ExpiredTokenException();
        }
    }

}
