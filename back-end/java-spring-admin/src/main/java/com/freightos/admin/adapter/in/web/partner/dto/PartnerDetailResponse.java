package com.freightos.admin.adapter.in.web.partner.dto;

import com.freightos.admin.domain.partner.entity.PartnerType;

import java.time.LocalDateTime;

public record PartnerDetailResponse(
        Long id,
        String partnerCode,
        PartnerType partnerType,
        String name,
        String nameEn,
        String businessNo,
        String representative,
        String phone,
        String email,
        String address,
        String memo,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
