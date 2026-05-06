package com.freightos.fms.adapter.in.web.airhouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchAirHouseRequest(
    @NotNull String bound,
    String dateKind,
    String dateFrom,
    String dateTo,
    String masterAwbKind,
    String masterAwbValue,
    String hblNo,
    String partyKind,
    String partyCode,
    String actualCustomerCode,
    String settlePartnerCode,
    String airlineCode,
    String portKind,
    String portCode,
    String shipmentType,
    String teamCode,
    String operatorCode,
    String salesClass,
    String salesManCode,
    String incoterms,
    @NotNull @Min(0) Integer page,
    @NotNull @Min(1) Integer size
) {}
