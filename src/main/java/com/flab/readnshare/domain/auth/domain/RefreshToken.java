package com.flab.readnshare.domain.auth.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;


/**
 * Redis에 저장되는 RefreshToken 정보를 담는 도메인 클래스.
 */
@RedisHash(value = "refreshToken")
@Getter
public class RefreshToken {
    // Redis에서 도메인 식별자로 사용되는 토큰 값
    @Id
    private String refreshTokenValue;

    // RefreshToken과 연관된 회원의 ID
    private Long memberId;

    // Redis에서의 만료 시간을 밀리초 단위로 설정
    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long expiration;

    @Builder
    public RefreshToken(String refreshTokenValue, Long memberId, Long expiration) {
        this.refreshTokenValue = refreshTokenValue;
        this.memberId = memberId;
        this.expiration = expiration;
    }
}
