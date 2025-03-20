package com.flab.readnshare.domain.member.dto;

import com.flab.readnshare.domain.member.domain.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class UpdateRequestDto {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickName;

    private String profileContent;

    @Builder
    public UpdateRequestDto(String password, String nickName, String profileContent) {
        this.password = password;
        this.nickName = nickName;
        this.profileContent = profileContent;
    }

    // Member 엔티티로 변환하는 메서드 추가
    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .nickName(nickName)
                .password(passwordEncoder.encode(password)) // 비밀번호 암호화
                .profileContent(profileContent)
                .build();
    }
}

