package com.flab.readnshare.domain.member.domain;

import com.flab.readnshare.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String password;
    private String nickName;
    private String profileContent;

    @Builder
    public Member(Long id, String email, String password, String nickName, String profileContent) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.profileContent = profileContent;
    }

    // 닉네임, 비밀번호 변경용 updateInfo 추가
    public void updateInfo(String nickName, String password, String profileContent) {
        this.nickName = nickName;
        this.password = password;
        this.profileContent = profileContent;
    }
}
