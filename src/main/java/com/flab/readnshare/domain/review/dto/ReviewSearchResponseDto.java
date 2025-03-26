package com.flab.readnshare.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewSearchResponseDto {
	@Schema(description = "리뷰 ID", example = "52")
	private Long reviewId;

	@Schema(description = "리뷰의 내용", example = "내용이 정말 재밌습니다.")
	private String content;

	@Schema(description = "리뷰한 책의 제목", example = "스프링 기초")
	private String bookTitle;

	@Schema(description = "리뷰한 책의 저자", example = "James Dean")
	private String bookAuthor;

	@Schema(description = "리뷰한 책의 출판사", example = "코딩출판")
	private String bookPublisher;

	@Schema(description = "리뷰한 회원의 이름", example = "readnshare")
	private String memberName;

}
