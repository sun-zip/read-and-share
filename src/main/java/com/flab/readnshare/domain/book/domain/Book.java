package com.flab.readnshare.domain.book.domain;

import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * Book 엔티티는 도서 정보를 저장하는 JPA 엔티티
 * 생성/수정 시간은 BaseTimeEntity를 상속받아 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(nullable = false)
    private String title;   // 도서 제목

    @Column(nullable = false, unique = true)
    private String isbn;    // 도서의 고유 ISBN

    private String image;   // 도서 이미지 URL

    private String author;  // 도서 저자

    private String publisher;   // 도서 출판사

    private String link; // 도서 관련 링크(상세 정보 페이지)

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description; // 도서 설명

    @OneToMany(mappedBy = "book")
    private List<Review> reviews = new ArrayList<>();   // 해당 도서에 작성된 리뷰 목록

    @Builder
    public Book(Long id, String title, String isbn, String image, String author, String publisher, String link, String description){
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.image = image;
        this.author = author;
        this.publisher = publisher;
        this.link = link;
        this.description = description;
    }
}
