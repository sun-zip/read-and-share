package com.flab.readnshare.domain.member.service;

import com.flab.readnshare.domain.member.domain.EmailToken;
import com.flab.readnshare.domain.member.domain.Image;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.dto.SignUpRequestDto;
import com.flab.readnshare.domain.member.dto.UpdateRequestDto;
import com.flab.readnshare.domain.member.repository.EmailTokenRepository;
import com.flab.readnshare.domain.member.repository.ImageRepository;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.global.common.exception.ImageException;
import com.flab.readnshare.global.common.exception.MemberException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;

    // 회원가입
    @Transactional
    public Member signUp(SignUpRequestDto dto) {
        validateDuplicateMember(dto.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // 계정을 비활성화한 상태로 저장
        Member member = Member.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickName(dto.getNickName())
                .build();

        memberRepository.save(member);

        // 이메일 인증 토큰 생성 및 저장
        String token = UUID.randomUUID().toString();
        EmailToken emailToken = EmailToken.builder()
                .email(dto.getEmail())
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(24)) // 24시간 유효
                .build();

        emailTokenRepository.save(emailToken);

        // 이메일 발송
        try {
            emailService.sendVerificationEmail(dto.getEmail(), token);
        } catch (MessagingException e) {
            // 예외 처리: 예를 들어, 회원가입 실패 처리
            throw new RuntimeException("이메일 인증 발송에 실패했습니다.", e);
        }
        return member;
    }

    // 이메일 인증
    @Transactional
    public void verifyEmail(String token) {
        EmailToken emailToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new MemberException.InvalidTokenException());

        if (emailToken.isExpired()) {
            throw new MemberException.ExpiredTokenException();
        }

        Member member = memberRepository.findByEmail(emailToken.getEmail())
                .orElseThrow(MemberException.MemberNotFoundException::new);

        member.setVerified(true); // 계정 활성화
        memberRepository.save(member);

        emailTokenRepository.delete(emailToken); // 인증 후 토큰 삭제

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

    public Image findByImageId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("이미지 ID는 null이 될 수 없습니다.");
        }

        return imageRepository.findById(id)
                .orElseThrow(ImageException.NotFoundImageException::new);
    }


    // 멤버 수정
    @Transactional
    public Member update(Long memberId, UpdateRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        // toEntity()를 사용하여 업데이트할 Member 객체 생성
        Member updatedMember = requestDto.toEntity(passwordEncoder);

        // 기존 member의 ID와 Email은 유지한 채 업데이트
        member.updateInfo(updatedMember.getNickName(),
                          updatedMember.getPassword(),
                          updatedMember.getProfileContent(),
                          updatedMember.getProfileImage());

        memberRepository.save(member); // 변경 감지로 자동 업데이트

        return member;
    }


    @Transactional
    public void delete(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
