package com.bobeat.backend.domain.member.controller;

import com.bobeat.backend.domain.member.dto.request.UpdateNicknameRequest;
import com.bobeat.backend.domain.member.dto.response.MemberProfileResponse;
import com.bobeat.backend.domain.member.dto.response.SavedStoreListResponse;
import com.bobeat.backend.domain.member.dto.response.UpdateNicknameResponse;
import com.bobeat.backend.domain.member.service.MemberService;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "프로필 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> getMyProfile(
            @Parameter(hidden = true) /*@AuthenticationPrincipal*/ Long memberId
    ) {
        MemberProfileResponse response = memberService.getMyProfile(memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "닉네임 수정", description = "사용자 닉네임을 수정합니다.")
    @PatchMapping("/me")
    public ApiResponse<UpdateNicknameResponse> updateNickname(
            @Parameter(hidden = true) /*@AuthenticationPrincipal*/ Long memberId,
            @RequestBody UpdateNicknameRequest request
    ) {
        UpdateNicknameResponse response = memberService.updateNickname(memberId, request);
        return ApiResponse.success(response);
    }
}
