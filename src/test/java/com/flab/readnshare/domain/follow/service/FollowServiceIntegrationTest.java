package com.flab.readnshare.domain.follow.service;

import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.follow.repository.FollowRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.FollowException;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class FollowServiceIntegrationTest {

    @Autowired
    FollowService followService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Nested
    @DisplayName("save 테스트")
    class saveTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member toMember = createMember("test1", "test");
            Member fromMember = createMember("test2", "test");
            memberRepository.saveAll(List.of(toMember, fromMember));

            // When
            Follow result = followService.save(fromMember, toMember);

            // Then
            assertThat(result.getToMember().getEmail()).isEqualTo(toMember.getEmail());
        }

        @Test
        @DisplayName("실패 - 중복 팔로우")
        void fail_duplicate_follow() throws Exception {
            // Given
            Member toMember = createMember("test1", "test");
            Member fromMember = createMember("test2", "test");
            memberRepository.saveAll(List.of(toMember, fromMember));
            Follow follow = createFollow(toMember, fromMember);
            followRepository.save(follow);

            // When
            // Then
            assertThatThrownBy(() -> followService.save(fromMember, toMember))
                    .isInstanceOf(FollowException.DuplicateFollowException.class)
                    .hasMessage("이미 해당 사용자를 팔로우하고 있습니다.");
        }
    }

    @Nested
    @DisplayName("delete 테스트")
    class deleteTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member toMember = createMember("test1", "test");
            Member fromMember = createMember("test2", "test");
            memberRepository.saveAll(List.of(toMember, fromMember));
            Follow follow = createFollow(toMember, fromMember);
            followRepository.save(follow);

            // When
            followService.delete(fromMember, toMember);

            // Then
            List<Follow> result = followRepository.findAll();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFollowerIds 테스트")
    class getFollowerIdsTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member toMember = createMember("test1", "test1");
            Member fromMember2 = createMember("test2", "test2");
            Member fromMember3 = createMember("test3", "test3");
            memberRepository.saveAll(List.of(toMember, fromMember2, fromMember3));
            Follow follow1 = createFollow(toMember, fromMember2);
            Follow follow2 = createFollow(toMember, fromMember3);
            followRepository.saveAll(List.of(follow1, follow2));

            // When
            List<Long> result = followService.getFollowerIds(toMember);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getFollowers 테스트")
    class getFollowersTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member toMember = createMember("test1", "test1");
            Member fromMember2 = createMember("test2", "test2");
            Member fromMember3 = createMember("test3", "test3");
            memberRepository.saveAll(List.of(toMember, fromMember2, fromMember3));
            Follow follow1 = createFollow(toMember, fromMember2);
            Follow follow2 = createFollow(toMember, fromMember3);
            followRepository.saveAll(List.of(follow1, follow2));

            // When
            List<Follow> result = followService.getFollowers(toMember);

            // Then
            assertThat(result).hasSize(2)
                    .extracting("fromMember.email", "fromMember.nickName")
                    .containsExactlyInAnyOrder(
                            Tuple.tuple(fromMember2.getEmail(), fromMember2.getNickName()),
                            Tuple.tuple(fromMember3.getEmail(), fromMember3.getNickName())
                    );
        }
    }

    @Nested
    @DisplayName("getFollowings 테스트")
    class getFollowingsTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member fromMember = createMember("test1", "test1");
            Member toMember1 = createMember("test2", "test2");
            Member toMember2 = createMember("test3", "test3");
            memberRepository.saveAll(List.of(fromMember, toMember1, toMember2));
            Follow follow1 = createFollow(toMember1, fromMember);
            Follow follow2 = createFollow(toMember2, fromMember);
            followRepository.saveAll(List.of(follow1, follow2));

            // When
            List<Follow> result = followService.getFollowings(fromMember);

            // Then
            assertThat(result).hasSize(2)
                    .extracting("toMember.email", "toMember.nickName")
                    .containsExactlyInAnyOrder(
                            Tuple.tuple(toMember1.getEmail(), toMember1.getNickName()),
                            Tuple.tuple(toMember2.getEmail(), toMember2.getNickName())
                    );
        }
    }

    private Member createMember(String email, String nickName) {
        return Member.builder()
                .email(email)
                .password(null)
                .nickName(nickName)
                .build();
    }

    private Follow createFollow(Member toMember, Member fromMember) {
        return Follow.builder()
                .toMember(toMember)
                .fromMember(fromMember)
                .build();
    }
}