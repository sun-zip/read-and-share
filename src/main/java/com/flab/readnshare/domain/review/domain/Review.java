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

    @Column(nullable = false)
    private Integer score;

    @Version
    private Long version;

    @Builder
    public Review(Long id, String content, Member member, Book book, Integer score) {
        this.id = id;
        this.content = content;
        this.member = member;
        this.book = book;
        this.score = validateAndDefaultScore(score);
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

    public Integer validateAndDefaultScore(Integer score){
        if(score == null) return 10;
        if(score < 0 || score > 10){
            throw new ReviewException.InvalidScoreException();
        }
        return score;
    }

    // [2025.3.20 코멘트]
    // update 함수는 Review의 content 필드를 수정하는 setter이다.
    // 하지만 함수명에서 그 의도가 분명히 보이지 않고 Service의 update와 혼동의 여지가 있으므로
    // updateContent로 리팩토링 하는 것이 좋아보임

//    public void update(String content) {
//        this.content = content;
//    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateScore(Integer score){
        this.score = validateAndDefaultScore(score);
    }
}
