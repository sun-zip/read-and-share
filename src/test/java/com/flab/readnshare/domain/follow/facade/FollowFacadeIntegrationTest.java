package com.flab.readnshare.domain.follow.facade;

import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.follow.repository.FollowRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
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
class FollowFacadeIntegrationTest {

    @Autowired
    FollowFacade followFacade;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Nested
    @DisplayName("unfollow 테스트")
    class unfollowTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member toMember = createMember("to", "test");
            Member fromMember = createMember("from", "test");
            memberRepository.saveAll(List.of(toMember, fromMember));
            String memberEmail = toMember.getEmail();

            Follow follow = createFollow(toMember, fromMember);
            followRepository.save(follow);

            // When
            followFacade.unfollow(memberEmail, fromMember);

            // Then
            List<Follow> result = followRepository.findAll();
            assertThat(result).isEmpty();
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