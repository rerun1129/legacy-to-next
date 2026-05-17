package com.freightos.admin.application.partner.command;

import com.freightos.admin.domain.partner.entity.PartnerType;

public record UpdatePartnerCommand(
        PartnerType partnerType,
        String name,
        String nameEn,
        String businessNo,
        String representative,
        String phone,
        String email,
        String address,
        String memo,
        boolean active
) {}
