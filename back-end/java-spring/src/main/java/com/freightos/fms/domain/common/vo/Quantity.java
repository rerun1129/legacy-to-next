package com.freightos.fms.domain.common.vo;

public record Quantity(Integer count) {

    public Quantity {
        if (count != null && count < 0) throw new IllegalArgumentException("Quantity count must be >= 0, got: " + count);
    }

    public static Quantity of(Integer count) {
        if (count == null) return null;
        return new Quantity(count);
    }
}
