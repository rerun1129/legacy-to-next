package com.freightos.fms.domain.common.vo;

import com.freightos.fms.domain.common.enums.PackageUnit;

public record CargoSummary(Quantity packageCount, PackageUnit packageUnit, Weight grossWeight, Volume volume) {
}
