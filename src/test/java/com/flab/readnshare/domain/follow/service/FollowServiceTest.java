package com.flab.readnshare.domain.follow.service;

import com.flab.readnshare.FollowTestFixture;
import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.follow.repository.FollowRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.exception.FollowException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {
    @Mock
    private FollowRepository followRepository;
    @InjectMocks
    private FollowService followService;

    @Test
    @DisplayName("이미 팔로우하고 있는 사용자를 또 팔로우하면 DuplicateFollowException이 발생한다")
    void follow_fail_duplicate_follow() {
        // given
        Member fromMember = FollowTestFixture.getMemberEntity();
        Member toMember = FollowTestFixture.getMemberEntity();

        Optional<Follow> follow = Optional.of(mock(Follow.class));
        when(followRepository.findByFromMemberAndToMember(fromMember, toMember)).thenReturn(follow);

        // when & then
        assertThrows(FollowException.DuplicateFollowException.class, () ->
                followService.save(fromMember, toMember));
    }

    @DisplayName("특정 사용자의 팔로우 목록을 조회한다.")
    @Test
    void getFollowers() throws Exception {
        // Given
        Member toMember = FollowTestFixture.getMemberEntity();
        Member fromMember1 = FollowTestFixture.getMemberEntity();
        Member fromMember2 = FollowTestFixture.getMemberEntity();
        Follow follow1 = FollowTestFixture.getFollowEntity(fromMember1, toMember);
        Follow follow2 = FollowTestFixture.getFollowEntity(fromMember2, toMember);

        given(followRepository.findByToMember(eq(toMember)))
                .willReturn(List.of(follow1, follow2));

        // When
        List<Follow> followers = followService.getFollowers(toMember);

        // Then
        Assertions.assertThat(followers).hasSize(2)
                .extracting("fromMember")
                .containsExactlyInAnyOrder(
                        fromMember1,
                        fromMember2
                );
    }

}