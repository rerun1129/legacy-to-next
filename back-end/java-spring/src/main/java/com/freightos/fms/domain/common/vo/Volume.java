package com.freightos.fms.domain.common.vo;

import java.math.BigDecimal;

public record Volume(BigDecimal cbm) {

    public Volume {
        if (cbm != null && cbm.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Volume cbm must be >= 0, got: " + cbm);
    }

    public static Volume of(BigDecimal cbm) {
        if (cbm == null) return null;
        return new Volume(cbm);
    }
}
