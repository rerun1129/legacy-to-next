package com.freightos.fms.application.airmaster.command;

public record SearchAirMasterCommand(
    String bound,
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
    Integer page,
    Integer size
) {}
