package com.flab.readnshare.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewSearchResponseDto {
	private Long reviewId;
	private String content;
	private String bookTitle;
	private String bookAuthor;
	private String bookPublisher;
	private String memberName;

}
