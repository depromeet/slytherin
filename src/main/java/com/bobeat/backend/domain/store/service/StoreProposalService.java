package com.bobeat.backend.domain.store.service;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.store.dto.request.EditProposalRequest;
import com.bobeat.backend.domain.store.dto.response.EditProposalResponse;
import com.bobeat.backend.domain.store.entity.Store;
import com.bobeat.backend.domain.store.entity.StoreProposal;
import com.bobeat.backend.domain.store.repository.StoreProposalRepository;
import com.bobeat.backend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreProposalService {

    private final StoreProposalRepository storeProposalRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public EditProposalResponse proposeEdit(Long memberId, Long storeId,
                                            EditProposalRequest request) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Store store = storeRepository.findByIdOrThrow(storeId);

        StoreProposal storeProposal = StoreProposal.builder()
                .proposalType(request.proposalType())
                .content(request.content())
                .member(member)
                .store(store)
                .build();

        StoreProposal saveStoreProposal = storeProposalRepository.save(storeProposal);
        return EditProposalResponse.from(saveStoreProposal);
    }

    @Transactional
    public EditProposalResponse proposeEditWithoutMember(Long storeId,
                                                         EditProposalRequest request) {
        Store store = storeRepository.findByIdOrThrow(storeId);

        StoreProposal storeProposal = StoreProposal.builder()
                .proposalType(request.proposalType())
                .content(request.content())
                .store(store)
                .build();

        StoreProposal saveStoreProposal = storeProposalRepository.save(storeProposal);
        return EditProposalResponse.from(saveStoreProposal);
    }
}
