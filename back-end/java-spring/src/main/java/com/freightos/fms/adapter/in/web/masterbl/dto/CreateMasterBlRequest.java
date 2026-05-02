package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Master B/L 신규 등록 요청 DTO. */
public record CreateMasterBlRequest(
        @NotNull MasterBlJobDiv jobDiv,
        @NotNull Bound bound,
        @Size(max = 35) String mblNo,
        @Size(max = 35) String masterRefNo,
        @NotNull FreightTerm freightTerm,
        @Size(max = 20) String shipperCode,
        @Size(max = 20) String consigneeCode,
        @Size(max = 5)  String polCode,
        @Size(max = 5)  String podCode,
        @Pattern(regexp = "\\d{8}") String etd,
        @Pattern(regexp = "\\d{8}") String eta,
        @Min(0) Integer pkgQty,
        @DecimalMin("0") BigDecimal grossWeightKg,
        @DecimalMin("0") BigDecimal cbm,
        String operatorCode
) {}
