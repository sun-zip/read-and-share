package com.flab.readnshare.domain.follow.service;

import com.flab.readnshare.FollowTestFixture;
import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.follow.repository.FollowRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.exception.FollowException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("follow 테스트")
    class followTest {

        @Test
        @DisplayName("실패 - 중복 팔로우")
        void fail_duplicate_follow() throws Exception {
            // given
            Member fromMember = FollowTestFixture.getMemberEntity();
            Member toMember = FollowTestFixture.getMemberEntity();

            Optional<Follow> follow = Optional.of(mock(Follow.class));
            when(followRepository.findByFromMemberAndToMember(fromMember, toMember)).thenReturn(follow);

            // when & then
            assertThrows(FollowException.DuplicateFollowException.class, () ->
                    followService.save(fromMember, toMember));
        }
    }

    @Nested
    @DisplayName("getFollowers 테스트")
    class getFollowersTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
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

    @Nested
    @DisplayName("getFollowings 테스트")
    class getFollowingsTest {
        @Test
        @DisplayName("특정 사용자의 팔로잉 목록을 조회한다.")
        void success() throws Exception {
            // Given
            Member fromMember = FollowTestFixture.getMemberEntity();
            Member toMember1 = FollowTestFixture.getMemberEntity();
            Member toMember2 = FollowTestFixture.getMemberEntity();
            Follow follow1 = FollowTestFixture.getFollowEntity(fromMember, toMember1);
            Follow follow2 = FollowTestFixture.getFollowEntity(fromMember, toMember2);

            given(followRepository.findByFromMember(eq(fromMember)))
                    .willReturn(List.of(follow1, follow2));

            // When
            List<Follow> followers = followService.getFollowings(fromMember);

            // Then
            Assertions.assertThat(followers).hasSize(2)
                    .extracting("toMember")
                    .containsExactlyInAnyOrder(
                            toMember1,
                            toMember2
                    );
        }
    }
}