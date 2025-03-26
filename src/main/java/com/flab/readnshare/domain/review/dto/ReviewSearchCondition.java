package com.flab.readnshare.domain.review.dto;

import lombok.Data;

@Data
public class ReviewSearchCondition {

	private String title;
	private String author;
	private String publisher;
	private String memberName;
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

}
