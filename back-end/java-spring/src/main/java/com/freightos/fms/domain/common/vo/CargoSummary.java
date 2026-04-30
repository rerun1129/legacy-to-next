package com.freightos.fms.domain.common.vo;

import com.freightos.fms.domain.common.enums.WeightUnit;

public record CargoSummary(Quantity packageCount, WeightUnit packageUnit, Weight grossWeight, Volume volume) {
}
