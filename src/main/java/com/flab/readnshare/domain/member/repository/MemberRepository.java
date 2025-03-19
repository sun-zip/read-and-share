package com.flab.readnshare.domain.member.repository;

import com.flab.readnshare.domain.member.domain.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    // UPDATE 문을 사용하기 위해 @Modifying 어노테이션 사용
    // jQuery 사용하여 email 제외 닉네임
    @Modifying
    @Query("UPDATE Member m SET m.nickName = :#{#member.nickName}, m.password = :#{#member.password} WHERE m.id = :id")
    void update(@Param("id") Long id, @Param("member") Member member);

    @Modifying
    @Query("DELETE FROM Member m WHERE m.id = :memberId")
    void deleteById(Long memberId);
}
