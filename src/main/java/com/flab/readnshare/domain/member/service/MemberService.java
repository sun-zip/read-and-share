package com.flab.readnshare.domain.member.service;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public Member signUp(SignUpRequestDto dto, PasswordEncoder passwordEncoder) {
        validateDuplicateMember(dto.getEmail());
        return memberRepository.save(dto.toEntity(passwordEncoder));
    }

    private void validateDuplicateMember(String email) {
        memberRepository.findByEmail(email).ifPresent(m -> {
            throw new MemberException.DuplicateEmailException();
        });
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberException.MemberNotFoundException::new);
    }

    // id 기반으로 멤버 조회
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(MemberException.MemberNotFoundException::new);
    }

    // 멤버 수정
    @Transactional
    public Member update(Long memberId, UpdateRequestDto requestDto, PasswordEncoder passwordEncoder) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        // toEntity()를 사용하여 업데이트할 Member 객체 생성
        Member updatedMember = requestDto.toEntity(passwordEncoder);

        // 기존 member의 ID와 Email은 유지한 채 업데이트
        member.updateInfo(updatedMember.getNickName(), updatedMember.getPassword(), updatedMember.getProfileContent());

        memberRepository.save(member); // 변경 감지로 자동 업데이트

        return member;
    }


    @Transactional
    public void delete(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
