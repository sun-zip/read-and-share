package com.flab.readnshare.domain.follow.facade;

import com.flab.readnshare.FollowTestFixture;
import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.follow.event.FollowEvent;
import com.flab.readnshare.domain.follow.service.FollowService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.MemberResponseDto;
import com.flab.readnshare.domain.member.service.MemberService;
import com.flab.readnshare.global.common.exception.FollowException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowFacadeTest {
    @Mock
    private MemberService memberService;
    @Mock
    private FollowService followService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private FollowFacade followFacade;

    @Test
    @DisplayName("팔로우에 성공한다")
    void follow_success() {
        // given
        String memberEmail = FollowTestFixture.getMemberEmail();

        Member fromMember = FollowTestFixture.getMemberEntity();
        Member toMember = FollowTestFixture.getMemberEntity();

        Follow expectedFollow = FollowTestFixture.getFollowEntity(fromMember, toMember);

        when(memberService.findByEmail(memberEmail)).thenReturn(toMember);
        when(followService.save(any(Member.class), any(Member.class))).thenReturn(expectedFollow);

        // when
        Follow savedFollow = followFacade.follow(memberEmail, fromMember);

        // then
        assertNotNull(savedFollow);
        assertEquals(savedFollow.getFromMember(), fromMember);
        assertEquals(savedFollow.getToMember(), toMember);

        verify(eventPublisher).publishEvent(any(FollowEvent.class));
    }

    @Test
    @DisplayName("자기자신을 팔로우하면 SelfFollowException이 발생한다")
    void follow_fail_self_follow() {
        // given
        String memberEmail = FollowTestFixture.getMemberEmail();
        Member fromMember = FollowTestFixture.getMemberEntity();

        when(memberService.findByEmail(memberEmail)).thenReturn(fromMember);

        // when & then
        assertThrows(FollowException.SelfFollowException.class, () ->
                followFacade.follow(memberEmail, fromMember));
    }

    @Test
    @DisplayName("언팔로우에 성공한다")
    void unfollow_success() {
        // given
        String memberEmail = FollowTestFixture.getMemberEmail();

        Member fromMember = FollowTestFixture.getMemberEntity();
        Member toMember = FollowTestFixture.getMemberEntity();

        when(memberService.findByEmail(memberEmail)).thenReturn(toMember);

        // when
        followFacade.unfollow(memberEmail, fromMember);

        // then
        verify(followService, times(1)).delete(fromMember, toMember);
    }

    @DisplayName("특정 유저의 팔로우 목록을 조회한다.")
    @Test
    void getFollowersOf() throws Exception {
        // Given
        Member toMember = FollowTestFixture.getMemberEntity();
        Member fromMember1 = FollowTestFixture.getMemberEntity();
        Member fromMember2 = FollowTestFixture.getMemberEntity();
        Follow follow1 = FollowTestFixture.getFollowEntity(fromMember1, toMember);
        Follow follow2 = FollowTestFixture.getFollowEntity(fromMember2, toMember);

        String memberEmail = toMember.getEmail();
        given(memberService.findByEmail(eq(memberEmail)))
                .willReturn(toMember);
        given(followService.getFollowers(eq(toMember)))
                .willReturn(List.of(follow1, follow2));

        // When
        List<MemberResponseDto> followers = followFacade.getFollowersOf(memberEmail);

        // Then
        Assertions.assertThat(followers).hasSize(2)
                .extracting("email")
                .containsExactlyInAnyOrder(
                        fromMember1.getEmail(),
                        fromMember2.getEmail()
                );
    }

    @DisplayName("특정 유저의 팔로잉 목록을 조회한다.")
    @Test
    void getFollowingsOf() throws Exception {
        // Given
        Member fromMember = FollowTestFixture.getMemberEntity();
        Member toMember1 = FollowTestFixture.getMemberEntity();
        Member toMember2 = FollowTestFixture.getMemberEntity();
        Follow follow1 = FollowTestFixture.getFollowEntity(fromMember, toMember1);
        Follow follow2 = FollowTestFixture.getFollowEntity(fromMember, toMember2);

        String memberEmail = fromMember.getEmail();
        given(memberService.findByEmail(eq(memberEmail)))
                .willReturn(fromMember);
        given(followService.getFollowings(eq(fromMember)))
                .willReturn(List.of(follow1, follow2));

        // When
        List<MemberResponseDto> followings = followFacade.getFollowingsOf(memberEmail);

        // Then
        Assertions.assertThat(followings).hasSize(2)
                .extracting("email")
                .containsExactlyInAnyOrder(
                        toMember1.getEmail(),
                        toMember2.getEmail()
                );
    }

}