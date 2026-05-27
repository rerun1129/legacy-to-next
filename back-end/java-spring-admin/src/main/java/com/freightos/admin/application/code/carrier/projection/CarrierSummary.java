package com.freightos.admin.application.code.carrier.projection;

import com.freightos.admin.domain.code.carrier.entity.CarrierType;

import java.time.LocalDateTime;

public record CarrierSummary(
        Long id,
        String carrierCode,
        String name,
        String nameEn,
        CarrierType carrierType,
        String carrierAddress,
        String ediCode,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
