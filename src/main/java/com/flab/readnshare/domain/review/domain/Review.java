package com.flab.readnshare.domain.review.domain;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.BaseTimeEntity;
import com.flab.readnshare.global.common.exception.ReviewException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_review_to_member"))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_review_to_book"))
    private Book book;

    @Version
    private Long version;

    @Builder
    public Review(Long id, String content, Member member, Book book) {
        this.id = id;
        this.content = content;
        this.member = member;
        this.book = book;
    }

    // [2025.3.17 코멘트]
    // 기존 코드에서 if 절의 조건인 this.member != member보다 equals를 오버라이딩하는게 맞지 않은가 싶음.
    // PK만 동일하다면 객체가 달라도 동일인으로 볼 수 있지 않은가?

//  <기존 코드>
//    public void verifyMember(Member member) {
//        if (this.member != member) {
//            throw new ReviewException.ForbiddenMemberException();
//        }
//    }

    public void verifyMember(Member member) {
        if (!this.member.equals(member)) {
            throw new ReviewException.ForbiddenMemberException();
        }
    }

    public void update(String content) {
        this.content = content;
    }
}
