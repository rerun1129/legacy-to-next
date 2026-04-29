package com.freightos.fms.domain.common.vo;

import java.math.BigDecimal;

public record Weight(BigDecimal kg) {

    public Weight {
        if (kg != null && kg.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Weight kg must be >= 0, got: " + kg);
    }

    public static Weight of(BigDecimal kg) {
        if (kg == null) return null;
        return new Weight(kg);
    }

    public Weight add(Weight other) {
        if (other == null) return this;
        return new Weight(this.kg.add(other.kg));
    }

    public boolean isZero() {
        return kg != null && kg.compareTo(BigDecimal.ZERO) == 0;
    }
}
