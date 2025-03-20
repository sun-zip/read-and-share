package com.flab.readnshare.global.common.auth.jwt;

import com.flab.readnshare.global.common.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * JWT 토큰 검증을 위한 인터셉터
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
//        if(requestURI.equals("/swagger-ui/") || requestURI.equals("/v3/api-docs/") || requestURI.equals("/swagger-resources/")){
//            return true;
//        }

        String accessToken = jwtUtil.extractAccessToken(request);

        if (accessToken != null) {
            switch (jwtUtil.validateToken(accessToken)) {
                case DENIED -> throw new AuthException.DeniedTokenException();
                case EXPIRED -> throw new AuthException.ExpiredTokenException();
                case ACCESS -> {
                    return true;
                }
            }
        } else {
            throw new AuthException.NullTokenException();
        }

        return false;
    }
}
