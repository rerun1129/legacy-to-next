package com.freightos.fms.domain.masterbl;

import com.freightos.fms.domain.common.enums.Bound;

public record MasterBlFilter(
    Bound bound,
    String mblNo,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etdFrom,
    String etdTo
) {}
