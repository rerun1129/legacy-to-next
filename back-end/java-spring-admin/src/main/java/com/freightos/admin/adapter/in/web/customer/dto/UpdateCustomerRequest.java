package com.freightos.admin.adapter.in.web.customer.dto;

import com.freightos.admin.domain.customer.entity.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotNull CustomerType customerType,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String nameEn,
        @Size(max = 50) String businessNo,
        @Size(max = 100) String representative,
        @Size(max = 50) String phone,
        @Email @Size(max = 200) String email,
        @Size(max = 500) String address,
        @Size(max = 500) String addressEn,
        @Size(max = 1000) String memo,
        @NotNull Boolean active
) {}
