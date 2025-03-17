package com.flab.readnshare.global.common.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 로그인한 회원 정보를 컨트롤러 메서드의 파라미터에 주입하기 위한 커스텀 어노테이션
 * 이 어노테이션이 붙은 파라미터는 SignInMemberArgumentResolver를 통해 처리
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SignInMember {
}
