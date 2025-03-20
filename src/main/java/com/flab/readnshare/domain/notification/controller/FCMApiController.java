package com.flab.readnshare.domain.notification.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.notification.dto.SaveFCMTokenRequestDto;
import com.flab.readnshare.domain.notification.service.FCMService;
import com.flab.readnshare.global.common.resolver.SignInMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcms")
@Tag(name = "Notification", description = "알림")
public class FCMApiController {
    private final FCMService fcmService;

    @PostMapping
    @Operation(summary = "FCM 토큰 저장", description = "Firebase Cloud Messaging에서 발급해주는 토큰을 서버에 저장할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT 토큰 없음", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    }))
    })
    public ResponseEntity<Void> saveFCMToken(@RequestBody SaveFCMTokenRequestDto dto, @Parameter(hidden = true) @SignInMember Member member) {
        fcmService.saveFCMToken(member, dto.getToken());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
