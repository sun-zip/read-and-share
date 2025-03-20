package com.flab.readnshare.domain.follow.controller;

import com.flab.readnshare.domain.follow.facade.FollowFacade;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.global.common.resolver.SignInMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follows")
public class FollowApiController {
    private final FollowFacade followFacade;

    @PostMapping("/{toMemberEmail}")
    public ResponseEntity<Void> follow(@PathVariable String toMemberEmail, @SignInMember Member fromMember) {
        followFacade.follow(toMemberEmail, fromMember);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{toMemberEmail}")
    public ResponseEntity<Void> unfollow(@PathVariable String toMemberEmail, @SignInMember Member fromMember) {
        followFacade.unfollow(toMemberEmail, fromMember);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{memberEmail}/followers")
    public ResponseEntity<List<MemberResponseDto>> followersOf(@PathVariable String memberEmail) {
        List<MemberResponseDto> followers = followFacade.getFollowersOf(memberEmail);
        return new ResponseEntity<>(followers, HttpStatus.OK);
    }

    @GetMapping("/{memberEmail}/followings")
    public ResponseEntity<List<MemberResponseDto>> followingsOf(@PathVariable String memberEmail) {
        List<MemberResponseDto> followings = followFacade.getFollowingsOf(memberEmail);
        return new ResponseEntity<>(followings, HttpStatus.OK);
    }
}
