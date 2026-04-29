package com.freightos.fms.domain.common.vo;

public record CargoSummary(Quantity packageCount, String packageUnit, Weight grossWeight, Volume volume) {
}
