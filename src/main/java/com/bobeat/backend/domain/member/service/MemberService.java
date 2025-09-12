package com.bobeat.backend.domain.member.service;

import com.bobeat.backend.domain.member.dto.request.UpdateNicknameRequest;
import com.bobeat.backend.domain.member.dto.response.MemberProfileResponse;
import com.bobeat.backend.domain.member.dto.response.SavedStoreListResponse;
import com.bobeat.backend.domain.member.dto.response.UpdateNicknameResponse;
import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
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

        return new MemberProfileResponse(
                member.getId().toString(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getOnboardingProfile().getHonbapLevel().getValue()
        );
    }

    @Transactional
    public UpdateNicknameResponse updateNickname(Long memberId, UpdateNicknameRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);

        member.updateNickname(request.nickname());
        
        return new UpdateNicknameResponse(request.nickname());
    }
}
