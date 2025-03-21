package com.flab.readnshare.domain.favorite.domain;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "book_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FavoriteBook extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String isbn;
    private String title;
    private String imageUrl;
    private String description;
}
