package com.flab.readnshare.domain.member.dto;

import com.flab.readnshare.domain.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberInfoResponseDto {
    private Long id;
    private String email;
    private String nickName;
    private String profileContent;
    private String profileImagePath;

    @Builder
    public MemberInfoResponseDto(Long id, String email, String nickName, String profileContent, String profileImagePath) {
        this.id = id;
        this.email = email;
        this.nickName = nickName;
        this.profileContent = profileContent;
        this.profileImagePath = profileImagePath;
    }
}
