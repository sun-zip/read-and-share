package com.flab.readnshare.domain.follow.repository;

import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FollowRepositoryTest {

    @Autowired
    FollowRepository followRepository;

    @Autowired
    MemberRepository memberRepository;

    @Nested
    @DisplayName("findByFromMemberAndToMember 테스트")
    class findByFromMemberAndToMemberTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            Member toMember = createMember("to");
            Member fromMember = createMember("from");
            memberRepository.saveAll(List.of(toMember, fromMember));

            Follow follow = createFollow(toMember, fromMember);

            followRepository.save(follow);

            // When
            Optional<Follow> optionalFollow = followRepository.findByFromMemberAndToMember(fromMember, toMember);
            assertThat(optionalFollow).isNotNull();

            Follow result = optionalFollow.get();
            // Then
            assertThat(result.getToMember().getEmail()).isEqualTo(toMember.getEmail());
            assertThat(result.getFromMember().getEmail()).isEqualTo(fromMember.getEmail());
        }
    }

    @Nested
    @DisplayName("findByToMember 테스트")
    class findByToMemberTest {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given
            List<Member> members = createMembers();
            memberRepository.saveAll(members);
            List<Member> findMembers = memberRepository.findAll();
            Member toMember = findMembers.get(0);
            Member fromMember1 = findMembers.get(1);
            Member fromMember2 = findMembers.get(2);
            Follow follow1 = createFollow(toMember, fromMember1);
            Follow follow2 = createFollow(toMember, fromMember2);
            followRepository.saveAll(List.of(follow1, follow2));

            // When
            List<Follow> follows = followRepository.findByToMember(toMember);

            // Then
            assertThat(follows).hasSize(2)
                    .extracting("fromMember.nickName")
                    .containsExactlyInAnyOrder(fromMember1.getNickName(), fromMember2.getNickName());
        }
    }

    @Nested
    @DisplayName("findByFromMember 테스트")
    class findByFromMemberTest {

        @DisplayName("성공")
        @Test
        void success() throws Exception {
            // Given
            List<Member> members = createMembers();
            memberRepository.saveAll(members);
            List<Member> findMembers = memberRepository.findAll();
            Member fromMember = findMembers.get(0);
            Member toMember1 = findMembers.get(1);
            Member toMember2 = findMembers.get(2);
            Follow follow1 = createFollow(toMember1, fromMember);
            Follow follow2 = createFollow(toMember2, fromMember);
            followRepository.saveAll(List.of(follow1, follow2));

            // When
            List<Follow> follows = followRepository.findByFromMember(fromMember);

            // Then
            assertThat(follows).hasSize(2)
                    .extracting("toMember.nickName")
                    .containsExactlyInAnyOrder(toMember1.getNickName(), toMember2.getNickName());
        }
    }

    private Follow createFollow(Member toMember, Member fromMember) {
        return Follow.builder()
                .toMember(toMember)
                .fromMember(fromMember)
                .build();
    }

    private Member createMember(String nickName) {
        return Member.builder()
                .email("test")
                .password(null)
                .nickName(nickName)
                .build();
    }

    private List<Member> createMembers() {
        Member member1 = createMember("test1");
        Member member2 = createMember("test2");
        Member member3 = createMember("test3");
        List<Member> members = List.of(member1, member2, member3);
        return members;
    }

}