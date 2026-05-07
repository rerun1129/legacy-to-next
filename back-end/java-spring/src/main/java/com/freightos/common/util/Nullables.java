package com.freightos.common.util;

import org.springframework.util.StringUtils;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Nullables {

    private Nullables() {}

    public static <T, R> R mapOrNull(T value, Function<T, R> fn) {
        return value != null ? fn.apply(value) : null;
    }

    public static <R> R mapIfHasText(String value, Function<String, R> fn) {
        return StringUtils.hasText(value) ? fn.apply(value) : null;
    }

    public static <T, R> R mapOrElse(T value, Function<T, R> fn, Supplier<R> fallback) {
        return value != null ? fn.apply(value) : fallback.get();
    }

    public static <T> T firstNonNull(T value, Supplier<T> fallback) {
        return value != null ? value : fallback.get();
    }
}
