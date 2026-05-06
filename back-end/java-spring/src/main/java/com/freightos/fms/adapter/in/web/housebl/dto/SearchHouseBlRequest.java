package com.freightos.fms.adapter.in.web.housebl.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchHouseBlRequest(
    @NotNull String jobDiv,
    String bound,
    String hblNo,
    String mblNo,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etdFrom,
    String etdTo,
    String linerCode,
    String partyCode,
    String portCode,
    String vessel,
    String voyage,
    String operatorCode,
    String teamCode,
    @NotNull @Min(0) Integer page,
    @NotNull @Min(1) Integer size,
    String dateKind,
    String partyKind,
    String portKind
) {}
