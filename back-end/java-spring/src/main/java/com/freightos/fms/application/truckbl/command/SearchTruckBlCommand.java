package com.freightos.fms.application.truckbl.command;

public record SearchTruckBlCommand(
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
    String dateKind,
    String partyKind,
    String portKind
) {}
