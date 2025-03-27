package com.flab.readnshare.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateReviewRequestDto {
    @NotEmpty(message = "내용을 입력해주세요.")
    @Schema(description = "수정된 리뷰의 내용")
    private String content;

    @Min(value = 0, message = "별점은 0점 이상이어야 합니다.")
    @Max(value = 10, message = "별점은 10점 이하여야 합니다.")
    @Schema(description = "수정된 리뷰의 별점. 기입하지 않는 경우 기존 별점 유지")
    private Integer score;

    @Builder
    public UpdateReviewRequestDto(String content, Integer score) {
        this.content = content;
        this.score = score;
    }
}
