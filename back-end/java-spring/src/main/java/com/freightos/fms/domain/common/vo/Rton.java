package com.freightos.fms.domain.common.vo;

import java.math.BigDecimal;

public record Rton(BigDecimal ton) {

    public Rton {
        if (ton != null && ton.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Rton must be >= 0, got: " + ton);
    }

    public static Rton of(BigDecimal ton) {
        if (ton == null) return null;
        return new Rton(ton);
    }
}
