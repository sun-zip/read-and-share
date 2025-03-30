package com.flab.readnshare.global.common.auth.jwt;

import com.flab.readnshare.domain.auth.domain.RefreshToken;
import com.flab.readnshare.domain.auth.repository.RefreshTokenRepository;
import com.flab.readnshare.global.common.exception.AuthException;
import com.flab.readnshare.global.common.util.CookieUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.access.expiration}")
    private Long accessTokenValidTime;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenValidTime;

    private final Key key;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 주어진 secretKey와 refreshTokenRepository를 사용하여 JwtUtil 객체를 생성
     *
     * @param secretKey JWT 서명을 위한 비밀 키
     * @param refreshTokenRepository 리프레시 토큰 저장소
     */
    @Autowired
    public JwtUtil(@Value("${jwt.security.secretKey}") String secretKey, RefreshTokenRepository refreshTokenRepository) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.refreshTokenRepository = refreshTokenRepository;
    }


    /**
     * 주어진 회원 ID에 대한 액세스 토큰을 생성
     *
     * @param memberId 회원의 ID
     * @return 생성된 액세스 토큰 문자열
     */
    public String createAccessToken(Long memberId) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(memberId));
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(new Date(now.getTime() + accessTokenValidTime))
                .compact();
    }

    /**
     * 주어진 회원 ID에 대한 리프레시 토큰을 생성하고, Redis에 저장
     *
     * @param memberId 회원의 ID
     * @return 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(Long memberId, String ipAddress) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(memberId));
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .compact();

        // Redis에 저장
        RefreshToken redisRefreshToken = RefreshToken.builder()
                .refreshTokenValue(refreshToken)
                .memberId(memberId)
                .ipAddress(ipAddress)
                .expiration(refreshTokenValidTime)
                .build();

        refreshTokenRepository.save(redisRefreshToken);

        return refreshToken;
    }

    /**
     * 주어진 JWT 토큰을 검증합니다.
     *
     * @param jwtToken 검증할 JWT 토큰
     * @return JwtCode 형태로 검증 결과 반환 (ACCESS, DENIED, EXPIRED)
     */
    public JwtCode validateToken(String jwtToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
            return JwtCode.ACCESS;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException | SignatureException ex) {
            return JwtCode.DENIED;
        } catch (ExpiredJwtException ex) {
            return JwtCode.EXPIRED;
        }
    }

    /**
     * HTTP 응답에 리프레시 토큰을 쿠키로 설정합니다.
     *
     * @param response HTTP 응답 객체
     * @param refreshToken 설정할 리프레시 토큰
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        CookieUtils.addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, refreshTokenValidTime.intValue() / 1000);
    }

    /**
     * HTTP 응답 헤더에 액세스 토큰을 설정합니다.
     *
     * @param response HTTP 응답 객체
     * @param accessToken 설정할 액세스 토큰
     */
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader("Authorization", BEARER_PREFIX + accessToken);
    }

    /**
     * HTTP 요청 쿠키에서 리프레시 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 리프레시 토큰
     * @throws AuthException.NullTokenException 리프레시 토큰을 찾을 수 없을 경우 예외 발생
     */
    public String extractRefreshToken(HttpServletRequest request) {
        Optional<Cookie> cookie = CookieUtils.getCookie(request, REFRESH_TOKEN_COOKIE);
        return cookie.map(Cookie::getValue).orElseThrow(AuthException.NullTokenException::new);
    }

    /**
     * HTTP 요청 헤더에서 액세스 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 액세스 토큰
     * @throws AuthException.NullTokenException 액세스 토큰을 찾을 수 없을 경우 예외 발생
     */
    public String extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .map(token -> token.substring(BEARER_PREFIX.length()))
                .orElseThrow(AuthException.NullTokenException::new);
    }

    /**
     * 주어진 토큰에서 회원 ID를 추출합니다.
     *
     * @param token 회원 ID를 추출할 토큰
     * @return 추출된 회원 ID (문자열)
     */
    public String extractMemberId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


}
