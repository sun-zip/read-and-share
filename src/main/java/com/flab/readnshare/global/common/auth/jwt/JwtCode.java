package com.flab.readnshare.global.common.auth.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * JWT 토큰 검증 결과 코드
 */
@Getter
@RequiredArgsConstructor
public enum JwtCode {
    EXPIRED,
    ACCESS,
    DENIED,
}
