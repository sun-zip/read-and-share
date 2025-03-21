package com.flab.readnshare.domain.review.dto;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SaveReviewRequestDto {
    @NotEmpty(message = "내용을 입력해주세요.")
    private String content;
    @Valid
    private BookDto book;

    @Min(value = 0, message = "별점은 0점 이상이어야 합니다.")
    @Max(value = 10, message = "별점은 10점 이하여야 합니다.")
    private Integer score;

    @Builder
    public SaveReviewRequestDto(String content, BookDto book, Integer score) {
        this.content = content;
        this.book = book;
        this.score = score;
    }

    public Review toEntity(Member member, Book book) {
        return Review.builder()
                .member(member)
                .book(book)
                .content(content)
                .score(this.score)
                .build();
    }
}
