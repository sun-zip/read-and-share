package com.flab.readnshare.domain.follow.facade;

import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.follow.event.FollowEvent;
import com.flab.readnshare.domain.follow.service.FollowService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.domain.member.service.MemberService;
import com.flab.readnshare.global.common.exception.FollowException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowFacade {
    private final MemberService memberService;
    private final FollowService followService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Follow follow(String memberEmail, Member fromMember) {
        Member toMember = memberService.findByEmail(memberEmail);

        if (fromMember == toMember) {
            throw new FollowException.SelfFollowException();
        }

        Follow follow = followService.save(fromMember, toMember);

        eventPublisher.publishEvent(new FollowEvent(this, fromMember, toMember));

        return follow;
    }

    @Transactional
    public void unfollow(String memberEmail, Member fromMember) {
        Member toMember = memberService.findByEmail(memberEmail);

        followService.delete(fromMember, toMember);
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> getFollowersOf(String memberEmail) {
        Member toMember = memberService.findByEmail(memberEmail);

        List<Follow> followers = followService.getFollowers(toMember);
        return followers.stream()
                .map(follow -> follow.getFromMember())
                .map(member -> MemberResponseDto.builder()
                        .id(member.getId())
                        .email(member.getEmail())
                        .nickName(member.getNickName())
                        .build())
                .toList();
    }
}
