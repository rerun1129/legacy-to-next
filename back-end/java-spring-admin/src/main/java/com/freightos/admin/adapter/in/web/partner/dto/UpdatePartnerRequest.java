package com.freightos.admin.adapter.in.web.partner.dto;

import com.freightos.admin.domain.partner.entity.PartnerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePartnerRequest(
        @NotNull PartnerType partnerType,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String nameEn,
        @Size(max = 50) String businessNo,
        @Size(max = 100) String representative,
        @Size(max = 50) String phone,
        @Email @Size(max = 200) String email,
        @Size(max = 500) String address,
        @Size(max = 1000) String memo,
        @NotNull Boolean active
) {}
