package com.freightos.fms.domain.common.enums;

public enum VolumeDivisor {
    CM6000("CM / 6000", 6000, "CM"),
    CM5000("CM / 5000", 5000, "CM"),
    CM5500("CM / 5500", 5500, "CM"),
    CM7000("CM / 7000", 7000, "CM"),
    CM7500("CM / 7500", 7500, "CM"),
    CM8000("CM / 8000", 8000, "CM"),
    IN166("INCH / 166", 166, "INCH"),
    IN366("INCH / 366", 366, "INCH");

    private final String label;
    private final int divisor;
    private final String unit;

    VolumeDivisor(String label, int divisor, String unit) {
        this.label = label;
        this.divisor = divisor;
        this.unit = unit;
    }

    public String getLabel() { return label; }
    public int getDivisor() { return divisor; }
    public String getUnit() { return unit; }
}
