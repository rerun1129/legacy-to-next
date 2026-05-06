package com.freightos.fms.adapter.in.web.airmaster.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchAirMasterRequest(
    @NotNull String bound,
    String dateKind,
    String dateFrom,
    String dateTo,
    String masterAwbKind,
    String masterAwbValue,
    String partyKind,
    String partyCode,
    String airlineCode,
    String portKind,
    String portCode,
    String shipmentType,
    String teamCode,
    @NotNull @Min(0) Integer page,
    @NotNull @Min(1) Integer size
) {}
