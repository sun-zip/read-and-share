package com.flab.readnshare.domain.feed.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedRequestDto {
    private Long lastReviewId;
    private Integer limit;
}
