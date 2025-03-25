package com.flab.readnshare.domain.member.controller;

import com.flab.readnshare.domain.member.domain.Image;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberInfoResponseDto;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "Member", description = "멤버 관리 API")
public class MemberApiController {
    private final MemberService memberService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "회원가입 성공", value = "{ \"id\": 1, \"email\": \"user@example.com\", \"nickName\": \"nickname\" }")})),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "입력값 오류", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")}))
    })
    public ResponseEntity<MemberResponseDto> signUp(@Valid @RequestBody SignUpRequestDto dto) {
        Member member = memberService.signup(dto);
        MemberResponseDto responseDto = new MemberResponseDto(member.getId(), member.getEmail(), member.getNickName());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/verification")
    @Operation(summary = "이메일 인증", description = "이메일 인증 토큰을 검증합니다.")
    @ApiResponse(responseCode = "200", description = "이메일 인증 성공")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        memberService.verifyEmail(token);
        return ResponseEntity.ok("이메일 인증이 완료되었습니다!");
    }

    @PutMapping("/{memberId}")
    @Operation(summary = "회원 정보 수정", description = "회원 정보를 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공")
    public ResponseEntity<MemberResponseDto> update(@PathVariable Long memberId, @Valid @RequestBody UpdateRequestDto dto) {
        Member updatedMember = memberService.update(memberId, dto);
        MemberResponseDto responseDto = new MemberResponseDto(updatedMember.getId(), updatedMember.getEmail(), updatedMember.getNickName());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    @Operation(summary = "회원 검색", description = "이메일을 기반으로 회원을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "회원 검색 성공")
    public ResponseEntity<MemberResponseDto> searchMember(@RequestParam String email) {
        Member member = memberService.findByEmail(email);
        MemberResponseDto responseDto = new MemberResponseDto(member.getId(), member.getEmail(), member.getNickName());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 조회", description = "회원 ID를 기반으로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음")
    })
    public ResponseEntity<?> findById(@PathVariable Long memberId) {
        Member member = memberService.findById(memberId);
        Image image = memberService.findByImageId(member.getProfileImage());

        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        MemberInfoResponseDto responseInfoDto = new MemberInfoResponseDto(
                member.getId(), member.getEmail(), member.getNickName(), member.getProfileContent(), image.getProfileImagePath()
        );

        return ResponseEntity.ok(responseInfoDto);
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "회원 삭제", description = "회원 ID를 기반으로 회원을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "회원 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ResponseEntity<Void> delete(@PathVariable Long memberId) {
        if (memberService.findById(memberId) == null) {
            return ResponseEntity.notFound().build();
        }
        memberService.delete(memberId);
        return ResponseEntity.noContent().build();
    }
}
