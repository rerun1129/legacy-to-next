package com.freightos.fms.application.seamaster.command;

public record SearchSeaMasterCommand(
    String bound,
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
    Integer page,
    Integer size
) {}
