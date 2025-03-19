package com.flab.readnshare.domain.follow.repository;

import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FollowRepositoryTest {

    @Autowired
    FollowRepository followRepository;

    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        followRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("특정 유저의 팔로우 목록을 조회한다.")
    @Test
    void findByToMember() throws Exception {
        // Given
        List<Member> members = createMembers();
        memberRepository.saveAll(members);
        List<Member> findMembers = memberRepository.findAll();
        Member toMember = createFollows(findMembers);

        // When
        List<Follow> follows = followRepository.findByToMember(toMember);

        // Then
        assertThat(follows).hasSize(2)
                .extracting("fromMember.nickName")
                .containsExactlyInAnyOrder("test2", "test3");
    }

    @DisplayName("특정 유저의 팔로잉 목록을 조회한다.")
    @Test
    void findByFromMember() throws Exception {
        // Given
        List<Member> members = createMembers();
        memberRepository.saveAll(members);
        List<Member> findMembers = memberRepository.findAll();
        Member fromMember = createFollowings(findMembers);

        // When
        List<Follow> follows = followRepository.findByFromMember(fromMember);

        // Then
        assertThat(follows).hasSize(2)
                .extracting("toMember.nickName")
                .containsExactlyInAnyOrder("test2", "test3");
    }

    private List<Member> createMembers() {
        Member member1 = createMember("test1");
        Member member2 = createMember("test2");
        Member member3 = createMember("test3");
        List<Member> members = List.of(member1, member2, member3);
        return members;
    }

    private Member createMember(String nickName) {
        return Member.builder()
                .email("test")
                .password(null)
                .nickName(nickName)
                .build();
    }

    private Member createFollows(List<Member> findMembers) {
        Member toMember = findMembers.get(0);
        for (int i = 1; i < findMembers.size(); i++) {
            Follow follow = Follow.builder()
                    .fromMember(findMembers.get(i))
                    .toMember(toMember)
                    .build();
            followRepository.save(follow);
        }
        return toMember;
    }

    private Member createFollowings(List<Member> findMembers) {
        Member fromMember = findMembers.get(0);
        for (int i = 1; i < findMembers.size(); i++) {
            Follow follow = Follow.builder()
                    .fromMember(fromMember)
                    .toMember(findMembers.get(i))
                    .build();
            followRepository.save(follow);
        }
        return fromMember;
    }

}