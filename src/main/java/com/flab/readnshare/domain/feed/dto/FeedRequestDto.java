package com.flab.readnshare.domain.feed.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FeedRequestDto {

    @Min(value = 1, message = "lastReviewId 값은 1 이상이어야 합니다.")
    private Long lastReviewId;

    @Min(value = 1, message = "limit 값은 1 이상이어야 합니다.")
    @Max(value = 10, message = "limit 값은 10 이하이어야 합니다.")
    private int limit;

    public FeedRequestDto() {
        limit = 10;
    }
}
