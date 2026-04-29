package com.freightos.fms.domain.housebl.enums;

public enum NoOfBl {
    ZERO(0), ONE(1), TWO(2), THREE(3);

    private final int number;

    NoOfBl(int number) { this.number = number; }

    public int getNumber() { return number; }

    public static NoOfBl fromNumber(Integer number) {
        if (number == null) return null;
        for (NoOfBl n : values()) {
            if (n.number == number) return n;
        }
        throw new IllegalArgumentException("Unknown NoOfBl number: " + number);
    }
}
