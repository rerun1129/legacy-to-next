package com.freightos.fms.application.nonbl.command;

public record SearchNonBlCommand(
    String bound,
    String hblNo,
    String etdFrom,
    String etdTo,
    String linerCode,
    String partyCode,
    String portCode,
    String vessel,
    String voyage,
    String operatorCode,
    String teamCode,
    String dateKind,
    String partyKind,
    String portKind
) {}
