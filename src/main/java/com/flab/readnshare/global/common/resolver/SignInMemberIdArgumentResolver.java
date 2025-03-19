package com.flab.readnshare.global.common.resolver;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.auth.jwt.JwtUtil;
import com.flab.readnshare.global.common.exception.MemberException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


/**
 * @SignInMember 어노테이션이 붙은 파라미터에 대해 로그인한 회원(Member 객체)을 주입하는 Argument Resolver
 *
 * 동작 과정:
 * 1. supportsParameter() 메서드를 통해, 파라미터가 @SignInMember 어노테이션을 가지고 있고 타입이 Member인지를 확인
 * 2. resolveArgument() 메서드에서는 HTTP 요청에서 액세스 토큰을 추출하고, 토큰에서 회원 ID를 얻은 후,
 *    데이터베이스에서 해당 회원 정보를 조회하여 컨트롤러 메서드의 파라미터로 주입
 */
@RequiredArgsConstructor
public class SignInMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    /**
     * 해당 파라미터가 @SignInMemberId 어노테이션을 가지고 있으며, 타입이 Long 인지 확인
     *
     * @param parameter 처리할 메서드 파라미터
     * @return 파라미터가 지원되면 true, 그렇지 않으면 false 반환
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SignInMemberId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    /**
     * HTTP 요청에서 액세스 토큰을 추출하고, 토큰에서 회원 ID를 얻은 후, 해당 회원 정보를 데이터베이스에서 조회하여 반환
     *
     * @param parameter   처리할 메서드 파라미터
     * @param mavContainer ModelAndViewContainer (사용되지 않음)
     * @param webRequest  HTTP 요청을 포함한 NativeWebRequest
     * @param binderFactory WebDataBinderFactory (사용되지 않음)
     * @return 로그인한 회원(Member 객체)
     * @throws Exception 회원 정보 조회에 실패할 경우 예외 발생
     */
    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // NativeWebRequest를 HttpServletRequest로 캐스팅
        HttpServletRequest httpServletRequest = (HttpServletRequest) webRequest.getNativeRequest();
        // HTTP 요청에서 액세스 토큰 추출
        String accessToken = jwtUtil.extractAccessToken(httpServletRequest);
        // 액세스 토큰에서 회원 ID 추출
        return Long.valueOf(jwtUtil.extractMemberId(accessToken));
    }
}
