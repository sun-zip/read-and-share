package com.flab.readnshare.domain.auth.controller;

import com.flab.readnshare.domain.auth.dto.SignInRequestDto;
import com.flab.readnshare.domain.auth.service.AuthService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.global.common.auth.jwt.JwtUtil;
import com.flab.readnshare.global.common.exception.AuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증")
public class AuthApiController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signIn")
    @Operation(summary = "로그인", description = "로그인하여 access token과 refresh token을 발급받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "파라미터 에러", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }"),
                    @ExampleObject(name = "아이디 또는 패스워드 에러", value = "{ \"code\": \"INVALID_EMAIL_OR_PASSWORD\", \"message\": \"이메일 또는 패스워드가 일치하지 않습니다.\" }"),
            }))
    })
    public ResponseEntity<MemberResponseDto> signIn(@RequestBody @Valid SignInRequestDto dto, HttpServletRequest request, HttpServletResponse response) {
        Member member = authService.signIn(dto);
        // access token, refresh token 발급
        authService.issueTokens(request, response, member.getId());
        return new ResponseEntity<>(MemberResponseDto.from(member), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "refresh token을 이용하여 access token을 재발급하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "리프레쉬 쿠키를 찾을 수 없음.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT 토큰 없음 ", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
            @ApiResponse(responseCode = "401", description = "토큰이 만료되었습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "토큰 만료", value = "{ \"code\": \"JWT_EXPIRED\", \"message\": \"만료된 토큰입니다.\" }"),
                            @ExampleObject(name = "형식에 맞지 않는 토큰", value = "{ \"code\": \"JWT_DENIED\", \"message\": \"조작되거나 지원되지 않는 토큰입니다.\" }")
                    }))
    })
    public ResponseEntity<Void> refresh(@CookieValue(value = "refreshToken") Cookie cookie, HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(cookie);
        Long memberId = extractMemberIdFromToken(refreshToken);

        // refresh token 검증
        switch (jwtUtil.validateToken(refreshToken)) {
            case DENIED -> throw new AuthException.DeniedTokenException();
            case EXPIRED -> throw new AuthException.ExpiredTokenException();
            case ACCESS -> {
                // refresh token 검증 후 access token 재발급
                authService.validateTokenFromRedis(refreshToken);
                // access token 재발급 및 리프레쉬 토큰 로테이션
                authService.updateRefreshToken(request, response, memberId, refreshToken);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    private Long extractMemberIdFromToken(String refreshToken) {
        String memberIdStr = Optional.ofNullable(refreshToken)
                .map(jwtUtil::extractMemberId)
                .orElseThrow(AuthException.DeniedTokenException::new);
        try {
            return Long.parseLong(memberIdStr);
        } catch (NumberFormatException e) {
            throw new AuthException.DeniedTokenException();
        }
    }

    private String extractRefreshTokenFromCookie(Cookie cookie) {
        return Optional.ofNullable(cookie)
                .map(Cookie::getValue)
                .orElseThrow(AuthException.NullTokenException::new);
    }
}