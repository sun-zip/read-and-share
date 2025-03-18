package com.flab.readnshare.global.config;

import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.auth.jwt.JwtAuthInterceptor;
import com.flab.readnshare.global.common.auth.jwt.JwtUtil;
import com.flab.readnshare.global.common.resolver.SignInMemberArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
//                 Exclusions for authentication
                .excludePathPatterns("/signUp", "/signIn", "/api/auth/refresh",
                        "/api/auth/signIn", "/api/member/signUp",
                        "/swagger-ui/**", "/v3/api-docs/**","/swagger-resources/**","/error");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SignInMemberArgumentResolver(jwtUtil, memberRepository));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
