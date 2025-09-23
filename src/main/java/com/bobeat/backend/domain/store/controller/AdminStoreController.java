package com.bobeat.backend.domain.store.controller;

import com.bobeat.backend.domain.store.dto.request.StoreCreateRequest;
import com.bobeat.backend.domain.store.dto.request.StoreUpdateRequest;
import com.bobeat.backend.domain.store.service.StoreService;
import com.bobeat.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "어드민 가게 관리", description = "어드민을 위한 가게 관리 API")
@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 등록", description = "어드민이 새로운 가게를 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<Long>> createStores(@Valid @RequestBody List<StoreCreateRequest> requests) {
        List<Long> storeIds = storeService.createStores(requests);
        return ApiResponse.success(storeIds);
    }

    @Operation(summary = "가게 정보 수정", description = "어드민이 기존 가게 정보를 수정합니다.")
    @PutMapping("/{storeId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Long> updateStore(@PathVariable Long storeId, @Valid @RequestBody StoreUpdateRequest request) {
        Long updatedStoreId = storeService.updateStore(storeId, request);
        return ApiResponse.success(updatedStoreId);
    }
}