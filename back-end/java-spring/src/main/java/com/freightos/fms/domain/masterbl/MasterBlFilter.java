package com.freightos.fms.domain.masterbl;

public record MasterBlFilter(
    com.freightos.fms.domain.common.enums.Bound bound,
    String mblNo,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etdFrom,
    String etdTo
) {}
