package com.freightos.fms.adapter.in.web.truckbl.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchTruckBlRequest(
    String bound,
    String truckBlNo,
    String etdFrom,
    String etdTo,
    String truckerCode,
    String docPartnerCode,
    String partyCode,
    String portCode,
    String operatorCode,
    String teamCode,
    @NotNull @Min(0) Integer page,
    @NotNull @Min(1) Integer size,
    String dateKind,
    String partyKind,
    String portKind
) {}
