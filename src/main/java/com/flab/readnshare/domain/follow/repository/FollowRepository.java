package com.flab.readnshare.domain.follow.repository;

import com.flab.readnshare.domain.follow.domain.Follow;
import com.flab.readnshare.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("SELECT f FROM Follow f JOIN FETCH f.fromMember JOIN FETCH f.toMember WHERE f.fromMember = :fromMember AND f.toMember = :toMember")
    Optional<Follow> findByFromMemberAndToMember(Member fromMember, Member toMember);

    @Query("SELECT f FROM Follow f JOIN FETCH f.toMember WHERE f.toMember = :member")
    List<Follow> findByToMember(Member member);

    @Query("SELECT f FROM Follow f JOIN FETCH f.fromMember WHERE f.fromMember = :member")
    List<Follow> findByFromMember(Member member);
}
