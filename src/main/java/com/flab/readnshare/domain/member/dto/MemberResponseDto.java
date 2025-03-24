package com.flab.readnshare.domain.member.dto;

import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberResponseDto {
    private Long id;
    private String email;
    private String nickName;

    @Builder
    public MemberResponseDto(Long id, String email, String nickName){
        this.id = id;
        this.email = email;
        this.nickName = nickName;
    }

    public Member toEntity(){
        return Member.builder()
                .id(id)
                .email(email)
                .nickName(nickName)
                .build();
    }

    public static MemberResponseDto fromMemberOf(Follow follow) {
        Member fromMember = follow.getFromMember();

        return MemberResponseDto.builder()
                .id(fromMember.getId())
                .email(fromMember.getEmail())
                .nickName(fromMember.getNickName())
                .build();
    }
    public static MemberResponseDto toMemberOf(Follow follow) {
        Member toMember = follow.getToMember();

        return MemberResponseDto.builder()
                .id(toMember.getId())
                .email(toMember.getEmail())
                .nickName(toMember.getNickName())
                .build();
    }

}
