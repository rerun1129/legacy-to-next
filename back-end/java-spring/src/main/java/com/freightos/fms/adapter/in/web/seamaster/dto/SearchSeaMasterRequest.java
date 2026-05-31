package com.freightos.fms.adapter.in.web.seamaster.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchSeaMasterRequest(
    @NotNull String bound,
    String dateKind,
    String dateFrom,
    String dateTo,
    String masterBlKind,
    String masterBlValue,
    String partyKind,
    String partyCode,
    String linerCode,
    String portKind,
    String portCode,
    String vesselName,
    String voyageNo,
    String shipmentType,
    String loadType,
    String teamCode,
    @NotNull @Min(0) Integer page,
    @NotNull @Min(1) Integer size
) {}
