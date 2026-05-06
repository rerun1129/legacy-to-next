package com.freightos.fms.adapter.in.web.masterbl.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Master B/L 검색 쿼리 파라미터 DTO. */
public record SearchMasterBlRequest(
        @NotNull String bound,
        @NotNull @Min(0) Integer page,
        @NotNull @Min(1) Integer size,
        String mblNo,
        String shipperCode,
        String consigneeCode,
        String polCode,
        String podCode,
        String etdFrom,
        String etdTo
) {}
