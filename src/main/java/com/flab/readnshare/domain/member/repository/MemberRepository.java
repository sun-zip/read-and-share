package com.flab.readnshare.domain.member.repository;

import com.flab.readnshare.domain.member.domain.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Modifying
    @Query("DELETE FROM Member m WHERE m.id = :memberId")
    void deleteById(Long memberId);
}
