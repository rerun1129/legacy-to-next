package com.freightos.admin.adapter.in.web.customer.dto;

import com.freightos.admin.domain.customer.entity.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String customerCode,
        @NotNull CustomerType customerType,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String nameEn,
        @Size(max = 50) String businessNo,
        @Size(max = 100) String representative,
        @Size(max = 50) String phone,
        @Email @Size(max = 200) String email,
        @Size(max = 4000) String customerLocalAddress,
        @Size(max = 4000) String customerEnglishAddress,
        @Size(max = 1000) String memo,
        @NotNull Boolean active
) {}
