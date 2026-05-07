package com.freightos.fms.adapter.in.web.seahouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchSeaHouseRequest(
    @NotNull String bound,
    String dateKind,
    String dateFrom,
    String dateTo,
    String masterBlKind,
    String masterBlValue,
    String hblNo,
    String partyKind,
    String partyCode,
    String actualCustomerCode,
    String partnerKind,
    String partnerCode,
    String linerCode,
    String portKind,
    String portCode,
    String vesselName,
    String voyageNo,
    String shipmentType,
    String teamCode,
    String operatorCode,
    String salesClass,
    String salesManCode,
    String incoterms,
    String loadType,
    @NotNull @Min(0) Integer page,
    @NotNull @Min(1) Integer size
) {}
