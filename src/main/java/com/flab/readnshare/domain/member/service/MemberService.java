package com.flab.readnshare.domain.member.service;

import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    /**
     * 회원가입
     */
    @Transactional
    public Member signUp(SignUpRequestDto dto) {
        validateDuplicateMember(dto.getEmail());
        return memberRepository.save(dto.toEntity());
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
    public Member update(Long id, UpdateRequestDto dto) {
        // 닉네임과 비밀번호 업데이트
        memberRepository.update(id, dto.getNickName(), dto.getPassword());

        // update 이후 id로 조회한 값 리턴
        return findById(id);
    }
}
