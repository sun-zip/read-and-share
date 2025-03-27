package com.flab.readnshare.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReviewSearchCondition {
	@Schema(description = "책 제목", example = "스프링 기초")
	private String title;

	@Schema(description = "책 작가", example = "James Dean")
	private String author;

	@Schema(description = "출판사", example = "코딩출판")
	private String publisher;

	@Schema(description = "리뷰 작성자 닉네임", example = "readnshare")
	private String memberName;

	@Schema(description = "통합 키워드", example = "스프링")
	private String keyword;


	public int countNonEmpty() {
		int count = 0;
		if (title != null && !title.isBlank()) count++;
		if (author != null && !author.isBlank()) count++;
		if (publisher != null && !publisher.isBlank()) count++;
		if (memberName != null && !memberName.isBlank()) count++;
		if (keyword != null && !keyword.isBlank()) count++;
		return count;
	}

	public String inputArgument() {
		if (getTitle() != null) return "title";
		if (getAuthor() != null) return "author";
		if (getPublisher() != null) return "publisher";
		if (getMemberName() != null) return "memberName";
		else return "keyword";
	}

}
