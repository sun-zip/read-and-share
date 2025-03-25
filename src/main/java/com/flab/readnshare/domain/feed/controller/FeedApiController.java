package com.flab.readnshare.domain.feed.controller;

import com.flab.readnshare.domain.feed.dto.FeedRequestDto;
import com.flab.readnshare.domain.feed.dto.FeedResponseDto;
import com.flab.readnshare.domain.feed.facade.FeedFacade;
import com.flab.readnshare.global.common.resolver.SignInMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "피드")
public class FeedApiController {
    private final FeedFacade feedFacade;

    @GetMapping
    @Operation(summary = "피드 조회", description = "자신의 피드를 조회할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 입력값", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    }))
    })
    public ResponseEntity<List<FeedResponseDto>> getFeeds(@ModelAttribute @Valid FeedRequestDto dto, @Parameter(hidden = true) @SignInMemberId Long memberId) {
        return new ResponseEntity<>(feedFacade.getFeeds(memberId, dto.getLastReviewId(), dto.getLimit()), HttpStatus.OK);
    }
}
