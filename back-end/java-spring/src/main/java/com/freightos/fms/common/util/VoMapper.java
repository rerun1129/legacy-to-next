package com.freightos.fms.common.util;

import java.util.function.Function;

public final class VoMapper {

    private VoMapper() {}

    public static <T, R> R mapOrNull(T vo, Function<T, R> fn) {
        return vo != null ? fn.apply(vo) : null;
    }
}
