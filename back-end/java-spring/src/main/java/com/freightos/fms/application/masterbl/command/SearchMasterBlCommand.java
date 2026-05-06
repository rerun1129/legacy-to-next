package com.freightos.fms.application.masterbl.command;

public record SearchMasterBlCommand(
    String bound,
    String mblNo,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etdFrom,
    String etdTo
) {}
