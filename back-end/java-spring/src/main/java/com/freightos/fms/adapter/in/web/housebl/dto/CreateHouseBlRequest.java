package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.common.enums.ShipmentType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateHouseBlRequest(
        @NotNull JobDiv jobDiv,
        @NotNull Bound bound,
        @Size(max = 35) String hblNo,
        @NotNull ShipmentType shipmentType,
        @NotNull FreightTerm freightTerm,
        @Size(max = 20) String shipperCode,
        @Size(max = 20) String consigneeCode,
        @Size(max = 20) String notifyCode,
        @Size(max = 5) String polCode,
        @Size(max = 5) String podCode,
        @Pattern(regexp = "\\d{8}") String etd,
        @Pattern(regexp = "\\d{8}") String eta,
        @Min(0) Integer pkgQty,
        WeightUnit pkgUnit,
        @DecimalMin("0") BigDecimal grossWeightKg,
        @DecimalMin("0") BigDecimal cbm,
        String operatorCode,
        String teamCode,
        String salesManCode,
        Long masterBlId
) {}
