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

    @Builder
    public MemberInfoResponseDto(Long id, String email, String nickName, String profileContent) {
        this.id = id;
        this.email = email;
        this.nickName = nickName;
        this.profileContent = profileContent;
    }

    public Member toEntity(){
        return Member.builder()
                .id(id)
                .email(email)
                .nickName(nickName)
                .profileContent(profileContent)
                .build();
    }

}
