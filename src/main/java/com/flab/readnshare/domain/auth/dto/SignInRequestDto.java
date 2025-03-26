package com.flab.readnshare.domain.auth.dto;

import com.flab.readnshare.domain.member.domain.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignInRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    @Builder
    public SignInRequestDto(String email, String password){
        this.email = email;
        this.password = password;
    }
}
