package com.freightos.fms.domain.housebl.enums;

public enum NoOfBl {
    ZERO(0, "ZERO(0)"),
    ONE(1, "ONE(1)"),
    TWO(2, "TWO(2)"),
    THREE(3, "THREE(3)");

    private final int number;
    private final String label;

    NoOfBl(int number, String label) {
        this.number = number;
        this.label  = label;
    }

    public int getNumber() { return number; }

    public String getLabel() { return label; }

    public static NoOfBl fromNumber(Integer number) {
        if (number == null) return null;
        for (NoOfBl n : values()) {
            if (n.number == number) return n;
        }
        throw new IllegalArgumentException("Unknown NoOfBl number: " + number);
    }
}
