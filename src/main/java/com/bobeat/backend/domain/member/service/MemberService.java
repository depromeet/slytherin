package com.bobeat.backend.domain.member.service;

import com.bobeat.backend.domain.member.dto.request.UpdateNicknameRequest;
import com.bobeat.backend.domain.member.dto.response.MemberProfileResponse;
import com.bobeat.backend.domain.member.dto.response.UpdateNicknameResponse;
import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberProfileResponse getMyProfile(Long memberId) {
        Member member = memberRepository.findByIdWithOnboardingProfileOrElseThrow(memberId);

        int honbobLevelValue = -1;
        if (member.getOnboardingProfile() != null) {
            honbobLevelValue = member.getOnboardingProfile().getHonbobLevel().getValue();
        }

        return new MemberProfileResponse(
                member.getId().toString(),
                member.getNickname(),
                member.getProfileImageUrl(),
                honbobLevelValue
        );
    }

    @Transactional
    public UpdateNicknameResponse updateNickname(Long memberId, UpdateNicknameRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);

        // 닉네임 중복 검사 (자신의 현재 닉네임은 제외)
        if (!member.getNickname().equals(request.nickname())
                && memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        member.updateNickname(request.nickname());

        return new UpdateNicknameResponse(request.nickname());
    }
}
