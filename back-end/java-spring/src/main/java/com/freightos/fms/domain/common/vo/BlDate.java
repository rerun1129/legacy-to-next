package com.freightos.fms.domain.common.vo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public record BlDate(LocalDate value) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public BlDate {
        Objects.requireNonNull(value, "BlDate value must not be null");
    }

    public static BlDate of(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
        try {
            return new BlDate(LocalDate.parse(yyyymmdd, FORMATTER));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format (expected yyyyMMdd): " + yyyymmdd, e);
        }
    }

    public String asString() {
        return value.format(FORMATTER);
    }

    public boolean isBeforeOrEqual(BlDate other) {
        Objects.requireNonNull(other);
        return !value.isAfter(other.value);
    }

    public boolean isAfter(BlDate other) {
        Objects.requireNonNull(other);
        return value.isAfter(other.value);
    }
}
