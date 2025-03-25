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
    private Long profileImage;
    private boolean isVerified;  // 이메일 인증 여부 추가

    @PrePersist
    public void setDefaultValues() {
        if (this.profileImage == null) {
            this.profileImage = 1L; // 기본값 설정
        }
    }

    @Builder
    public Member(Long id, String email, String password, String nickName, String profileContent, Long profileImage) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.profileContent = profileContent;
        this.profileImage = profileImage;
        this.isVerified = false;
    }

    // 닉네임, 비밀번호 변경용 updateInfo 추가
    public void updateInfo(String nickName, String password, String profileContent, Long profileImage) {
        this.nickName = nickName;
        this.password = password;
        this.profileContent = profileContent;
        this.profileImage = profileImage;
    }

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id.equals(member.id);
    }
}
