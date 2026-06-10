package com.freightos.pms.adapter.out.mart;

/**
 * PmsMartCountResolver가 결정한 총건수와 근사 여부를 묶는 값 객체.
 *
 * approximate=true인 경우: $sample 기반 몽고 근사 추정치(putApprox 분기).
 * approximate=false인 경우: 정확한 count — 캐시 exact 히트, Redis count-index,
 *   Mongo exact, 희소 폴백, line-accel OFF 경로 전부.
 */
record ResolvedTotal(long total, boolean approximate) {

    /** 정확 count 결과를 감싼다. */
    static ResolvedTotal exact(long total) {
        return new ResolvedTotal(total, false);
    }

    /** 근사($sample) count 결과를 감싼다. */
    static ResolvedTotal approx(long total) {
        return new ResolvedTotal(total, true);
    }
}
