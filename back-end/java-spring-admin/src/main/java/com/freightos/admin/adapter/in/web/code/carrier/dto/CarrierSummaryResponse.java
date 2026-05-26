package com.freightos.admin.adapter.in.web.code.carrier.dto;

import com.freightos.admin.domain.code.carrier.entity.CarrierType;

import java.time.LocalDateTime;

public record CarrierSummaryResponse(
        Long id,
        String carrierCode,
        String name,
        CarrierType carrierType,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
