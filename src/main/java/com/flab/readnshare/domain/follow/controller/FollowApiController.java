package com.flab.readnshare.domain.follow.controller;

import com.flab.readnshare.domain.follow.facade.FollowFacade;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.global.common.resolver.SignInMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follows")
@Tag(name = "Follow", description = "팔로우")
public class FollowApiController {
    private final FollowFacade followFacade;

    @PostMapping("/{toMemberEmail}")
    @Operation(summary = "팔로우", description = "특정 사용자를 팔로우할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT 토큰 없음", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "자기 자신 팔로우", value = "{ \"code\": \"SELF_FOLLOW\", \"message\": \"자기 자신을 팔로우 할 수 없습니다.\" }"),
                                    @ExampleObject(name = "중복 팔로우", value = "{ \"code\": \"FOLLOW_DUPLICATION\", \"message\": \"이미 해당 사용자를 팔로우하고 있습니다.\" }"),
                                    @ExampleObject(name = "올바르지 않은 이메일 형식", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")
                            })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "존재하지 않는 회원 조회", value = "{ \"code\": \"MEMBER_NOT_FOUND\", \"message\": \"존재하지 않는 회원입니다.\" }")
                    }))
    })
    @Parameter(name = "toMemberEmail", description = "팔로우 할 사용자 이메일", required = true)
    public ResponseEntity<Void> follow(
            @PathVariable @Email(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$") String toMemberEmail,
            @Parameter(hidden = true) @SignInMember Member fromMember) {
        followFacade.follow(toMemberEmail, fromMember);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    @DeleteMapping("/{toMemberEmail}")
    @Operation(summary = "언팔로우", description = "특정 사용자를 언팔로우할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 이메일 형식", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "존재하지 않는 회원 조회", value = "{ \"code\": \"MEMBER_NOT_FOUND\", \"message\": \"존재하지 않는 회원입니다.\" }")
                    }))
    })
    @Parameter(name = "toMemberEmail", description = "언팔로우 할 사용자 이메일", required = true)
    public ResponseEntity<Void> unfollow(@PathVariable @Email(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$") String toMemberEmail, @Parameter(hidden = true) @SignInMember Member fromMember) {
        followFacade.unfollow(toMemberEmail, fromMember);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{memberEmail}/followers")
    @Operation(summary = "팔로워 조회", description = "특정 사용자의 팔로워를 조회할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 이메일 형식", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "존재하지 않는 회원 조회", value = "{ \"code\": \"MEMBER_NOT_FOUND\", \"message\": \"존재하지 않는 회원입니다.\" }")
                    }))
    })
    @Parameter(name = "memberEmail", description = "팔로워 조회할 사용자 이메일", required = true)
    public ResponseEntity<List<MemberResponseDto>> followersOf(@PathVariable @Email(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$") String memberEmail) {
        List<MemberResponseDto> followers = followFacade.getFollowersOf(memberEmail);
        return new ResponseEntity<>(followers, HttpStatus.OK);
    }

    @GetMapping("/{memberEmail}/followings")
    @Operation(summary = "팔로잉 조회", description = "특정 사용자의 팔로잉을 조회할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 이메일 형식", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "존재하지 않는 회원 조회", value = "{ \"code\": \"MEMBER_NOT_FOUND\", \"message\": \"존재하지 않는 회원입니다.\" }")
                    }))
    })
    @Parameter(name = "memberEmail", description = "팔로잉 조회할 사용자 이메일", required = true)
    public ResponseEntity<List<MemberResponseDto>> followingsOf(@PathVariable @Email(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$") String memberEmail) {
        List<MemberResponseDto> followings = followFacade.getFollowingsOf(memberEmail);
        return new ResponseEntity<>(followings, HttpStatus.OK);
    }
}
