package com.flab.readnshare.domain.review.dto;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SaveReviewRequestDto {
    @NotEmpty(message = "내용을 입력해주세요.")
    @Schema(description = "리뷰 내용", example = "내용이 정말 재밌습니다.")
    private String content;

    @Valid
    @Schema(description = "리뷰한 책의 정보", implementation = BookDto.class)
    private BookDto book;

    @Min(value = 0, message = "별점은 0점 이상이어야 합니다.")
    @Max(value = 10, message = "별점은 10점 이하여야 합니다.")
    @Schema(description = "리뷰의 별점. 0점 ~ 10점. 별 반개당 1점으로 총 별 다섯개 만점", example = "5")
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
