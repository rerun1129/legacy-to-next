package com.freightos.fms.adapter.in.web.switchbl.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Switch B/L 신규 등록 요청 DTO. */
public record CreateSwitchBlRequest(
        @NotNull Long houseBlId,
        @Size(max = 50) String switchBlNo,
        @Size(max = 15) String blType,
        @Size(max = 10) String incoterms,
        @NotBlank @Size(max = 20) String shipperCode,
        @Size(max = 500) String shipperAddress,
        @Size(max = 20) String consigneeCode,
        @Size(max = 500) String consigneeAddress,
        @Size(max = 20) String notifyCode,
        @Size(max = 500) String notifyAddress,
        @Valid SwitchBlDescriptionDto description
) {}
