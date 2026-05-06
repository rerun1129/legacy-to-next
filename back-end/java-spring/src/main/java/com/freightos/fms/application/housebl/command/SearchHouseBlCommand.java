package com.freightos.fms.application.housebl.command;

public record SearchHouseBlCommand(
    String jobDiv,
    String bound,
    String hblNo,
    String mblNo,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etdFrom,
    String etdTo,
    String vessel,
    String voyage,
    String linerCode,
    String operatorCode,
    String teamCode,
    String partyCode,
    String portCode,
    String dateKind,
    String partyKind,
    String portKind
) {}
