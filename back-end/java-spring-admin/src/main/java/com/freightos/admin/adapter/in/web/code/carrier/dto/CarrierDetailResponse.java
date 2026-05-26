package com.freightos.admin.adapter.in.web.code.carrier.dto;

import com.freightos.admin.domain.code.carrier.entity.CarrierType;

import java.time.LocalDateTime;

public record CarrierDetailResponse(
        Long id,
        String carrierCode,
        String name,
        String nameEn,
        CarrierType carrierType,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
