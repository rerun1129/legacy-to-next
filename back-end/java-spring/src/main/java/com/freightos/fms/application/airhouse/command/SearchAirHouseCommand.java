package com.freightos.fms.application.airhouse.command;

public record SearchAirHouseCommand(
    String bound,
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
    Integer page,
    Integer size
) {}
