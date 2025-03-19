package com.flab.readnshare.domain.member.controller;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberApiController {
    private final MemberService memberService;

    // 회원가입
    @PostMapping("/signUp")
    public ResponseEntity<MemberResponseDto> signUp(@Valid @RequestBody SignUpRequestDto dto){
        Member newMember = memberService.signUp(dto);

        MemberResponseDto responseDto = MemberResponseDto.builder()
                .id(newMember.getId())
                .email(newMember.getEmail())
                .nickName(newMember.getNickName())
                .build();

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }


    @GetMapping("/search")
    public ResponseEntity<MemberResponseDto> searchMember(@Valid @RequestParam String email){
        Member member = memberService.findByEmail(email);

        MemberResponseDto responseDto = MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .build();

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
    // 회원수정
    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponseDto> update(@PathVariable Long memberId, @Valid @RequestBody UpdateRequestDto dto){
        Member updatedMember = memberService.update(memberId, dto);

        MemberResponseDto responseDto = MemberResponseDto.builder()
                .id(updatedMember.getId())
                .email(updatedMember.getEmail())
                .nickName(updatedMember.getNickName())
                .build();

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    // 회원조회
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDto> findById(@PathVariable Long memberId) {
        Member member = memberService.findById(memberId);

        MemberResponseDto responseDto = MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .build();

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> delete(@PathVariable Long memberId) {
        // 회원이 없을 경우
        if (memberService.findById(memberId) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        memberService.delete(memberId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}