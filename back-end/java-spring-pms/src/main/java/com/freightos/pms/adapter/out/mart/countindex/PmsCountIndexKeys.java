package com.freightos.pms.adapter.out.mart.countindex;

import java.util.Arrays;
import java.util.List;

/**
 * Redis Count Index 키 스키마 헬퍼.
 *
 * prefix는 호출측(PmsCountIndexMaintainer, PmsRedisExactCountProvider)에서 주입한다.
 * 모든 메서드는 순수 문자열 조합이므로 Spring 빈이 아니다.
 *
 * 키 레이아웃:
 * - {p}:bl:{dim}:{code}                    — dim별 코드 비트맵 (byte[])
 * - {p}:bl:has:{flag}                      — basis 존재 플래그 비트맵 (byte[])
 * - {p}:bl:etd:{date}                      — ETD 일별 버킷 비트맵 (byte[])
 * - {p}:bl:eta:{date}                      — ETA 일별 버킷 비트맵 (byte[])
 * - {p}:ln:pd:{day}:{attr}                 — line(perfdt) 일·속성 버킷 비트맵 (byte[])
 * - {p}:ln:pd:{day}:c:{t}{s}{i}:{TYPE}     — E3 composite 일·복합 버킷 비트맵 (byte[])
 * - {p}:ln:c:{t}{s}{i}:{TYPE}              — E3 전역(무pd) composite 버킷 비트맵 (byte[])
 * - {p}:ln:fdc:{TYPE}                      — line 전역 서류타입 버킷 비트맵 (byte[])
 * - {p}:bl:dcx:status:{STATUS}             — W3 doc-exists: status=S인 doc ≥1 보유 B/L 비트맵 (byte[])
 * - {p}:meta                               — 메타 해시 (complete, syncAt)
 * - {p}:bl:overflow                        — B/L ordinal overflow 플래그 (존재 시 not-ready)
 *
 * W1-A: FE가 전송하지 않는 차원(cust/spc/liner/pol/pod/salesman/houseteam/salesclass/incoterms)
 *        관련 상수·메서드 제거. jobDiv/bound 차원만 잔존.
 * W2: E3 composite 일버킷({p}:ln:pd:{day}:c:{t}{s}:{TYPE}) 및
 *     전역 composite({p}:ln:c:{t}{s}:{TYPE}) 키 메서드 추가. 2-bit(t/s) 인코딩.
 * W3: B/L-grain doc-exists 비트맵 키 blDocStatusBitmap 추가.
 */
final class PmsCountIndexKeys {

    // ── dim 코드 상수 (W1-A: jobDiv/bound만 잔존) ───────────────────────────────

    static final String DIM_JOBDIV  = "jobdiv";
    static final String DIM_BOUND   = "bound";

    // ── has-flag 상수 ────────────────────────────────────────────────────────

    static final String FLAG_FREIGHT = "freight";
    static final String FLAG_TAX     = "tax";
    static final String FLAG_SLIP    = "slip";
    static final String FLAG_DOC     = "doc";

    // ── fdcType 도메인 상수 (PmsBlLineEmbedded.fdcType 값 집합) ──────────────
    // Maintainer.addLineKeys가 fdcType 그대로 버킷에 인코딩하므로 이 상수가 인코딩 결과와 동일하다.

    static final String FDC_TYPE_INVOICE = "INVOICE";
    static final String FDC_TYPE_DEBIT   = "DEBIT";
    static final String FDC_TYPE_PAYMENT = "PAYMENT";
    static final String FDC_TYPE_CREDIT  = "CREDIT";

    /**
     * documentTypes 미지정(무제약) 시 열거할 fdcType 도메인 전체.
     * null 원소는 "none" 버킷 — pd/lines가 있지만 fdcType이 설정되지 않은 라인을 포함한다.
     * List.of는 null 원소 금지(NPE)이므로 Arrays.asList를 사용한다.
     */
    static final List<String> FDC_ALL_TYPES = Arrays.asList(
            FDC_TYPE_INVOICE, FDC_TYPE_DEBIT, FDC_TYPE_PAYMENT, FDC_TYPE_CREDIT, null
    );

    // ── line 속성 상수 (perfdt 버킷 attr 부분) ────────────────────────────────

    /** perfdt 일버킷: 모든 freight 라인(pd 비공백) 공통 속성. */
    static final String LINE_ATTR_HAS_FREIGHT = "has-freight";
    /** perfdt 일버킷: tax_no IS NOT NULL 라인 속성. */
    static final String LINE_ATTR_HAS_TAX     = "has-tax";
    /** perfdt 일버킷: slip_no IS NOT NULL 라인 속성. */
    static final String LINE_ATTR_HAS_SLIP    = "has-slip";
    /** 전역 서류타입 버킷 키 prefix(fdc- 뒤 타입 코드). */
    static final String LINE_FDC_PREFIX       = "fdc-";

    // ── 메타 필드 상수 ───────────────────────────────────────────────────────

    static final String META_SYNC_AT  = "syncAt";
    /** "1" 이 저장되어 있을 때만 인덱스가 완전히 빌드된 상태. */
    static final String META_COMPLETE = "complete";

    /**
     * B/L ordinal 상한값. blId > ORDINAL_MAX_BL_ID 이면 overflow 처리.
     * ordinal = blId * 2 + (MASTER ? 1 : 0). MAX 값: Integer.MAX_VALUE = 2^31-1.
     * blId 최대값: (Integer.MAX_VALUE - 1) / 2 = 1,073,741,823
     */
    static final long ORDINAL_MAX_BL_ID = (long)(Integer.MAX_VALUE - 1) / 2;

    private PmsCountIndexKeys() {}

    // ── ordinal 산식 (결정적 — Redis 저장 불요) ──────────────────────────────

    /**
     * blId·blType에서 정수 ordinal을 결정적으로 파생한다.
     * HOUSE blId=N → N*2, MASTER blId=N → N*2+1.
     * overflow 조건: blId null/음수 또는 blId > ORDINAL_MAX_BL_ID.
     */
    static int toOrdinal(long blId, String blType) {
        return (int) (blId * 2 + ("MASTER".equals(blType) ? 1 : 0));
    }

    static boolean isBlIdOverflow(Long blId) {
        return blId == null || blId < 0 || blId > ORDINAL_MAX_BL_ID;
    }

    // ── 키 생성 메서드 ────────────────────────────────────────────────────────

    static String dimBitmap(String prefix, String dim, String code) {
        return prefix + ":bl:" + dim + ":" + code;
    }

    static String hasFlagBitmap(String prefix, String flag) {
        return prefix + ":bl:has:" + flag;
    }

    static String etdDayBitmap(String prefix, String yyyyMMdd) {
        return prefix + ":bl:etd:" + yyyyMMdd;
    }

    static String etaDayBitmap(String prefix, String yyyyMMdd) {
        return prefix + ":bl:eta:" + yyyyMMdd;
    }

    /**
     * B/L ordinal overflow 감지 플래그 키.
     * 존재 시 isReady()=false → Mongo 폴백.
     * 예: {p}:bl:overflow
     */
    static String blOverflowFlag(String prefix) {
        return prefix + ":bl:overflow";
    }

    static String meta(String prefix) {
        return prefix + ":meta";
    }

    /** Redis SCAN 패턴 — prefix 전체 삭제용. */
    static String scanPattern(String prefix) {
        return prefix + ":*";
    }

    // ── line(perfdt) 버킷 키 생성 메서드 ──────────────────────────────────────

    /**
     * perfdt 일·속성 버킷 키.
     * 예: {p}:ln:pd:20240115:has-freight
     */
    static String linePdAttrBitmap(String prefix, String day, String attr) {
        return prefix + ":ln:pd:" + day + ":" + attr;
    }

    /**
     * line 전역 서류타입 버킷 키.
     * 예: {p}:ln:fdc:INVOICE
     */
    static String lineGlobalFdcBitmap(String prefix, String fdcType) {
        return prefix + ":ln:fdc:" + fdcType;
    }

    // ── E3 composite 버킷 키 (W2) ─────────────────────────────────────────────

    /**
     * E3 복합 일버킷 키.
     * 라인 하나에 대해 (pd일자, tax여부, slip여부, fdcType)을 모두 인코딩한다.
     * 2-bit(t/s)는 "0" 또는 "1". TYPE은 fdcType 또는 "none".
     *
     * 예: {p}:ln:pd:20240115:c:01:INVOICE
     *     {p}:ln:pd:20240115:c:00:none
     */
    static String lineCompositePdBitmap(String prefix, String day, boolean tax, boolean slip, String fdcType) {
        return prefix + ":ln:pd:" + day + ":c:" + encodeTs(tax, slip) + ":" + encodeType(fdcType);
    }

    /**
     * E3 전역(pd 없는 라인) 복합 버킷 키.
     * pd 값이 null/공백인 라인은 일버킷에 포함되지 않으나 전역 집계에는 포함된다.
     *
     * 예: {p}:ln:c:01:INVOICE
     */
    static String lineCompositeGlobalBitmap(String prefix, boolean tax, boolean slip, String fdcType) {
        return prefix + ":ln:c:" + encodeTs(tax, slip) + ":" + encodeType(fdcType);
    }

    // ── doc(fdId-grain) 버킷 키 생성 메서드 ─────────────────────────────────

    /**
     * doc 전체 비트맵 — 멤버: (int) fdId.
     * 예: {p}:dc:all
     */
    static String docAllBitmap(String prefix) {
        return prefix + ":dc:all";
    }

    /**
     * doc 서류타입 비트맵.
     * 예: {p}:dc:type:INVOICE
     */
    static String docTypeBitmap(String prefix, String docType) {
        return prefix + ":dc:type:" + docType;
    }

    /**
     * doc 상태 비트맵.
     * 예: {p}:dc:status:ISSUED
     */
    static String docStatusBitmap(String prefix, String status) {
        return prefix + ":dc:status:" + status;
    }

    /**
     * doc 서류일자(docDt) 일버킷 비트맵.
     * 예: {p}:dc:docdt:20240115
     */
    static String docDtDayBitmap(String prefix, String yyyyMMdd) {
        return prefix + ":dc:docdt:" + yyyyMMdd;
    }

    /**
     * doc 실적일자(perfPd) 일버킷 비트맵.
     * 예: {p}:dc:perfpd:20240115
     */
    static String docPerfPdDayBitmap(String prefix, String yyyyMMdd) {
        return prefix + ":dc:perfpd:" + yyyyMMdd;
    }

    /**
     * doc fdId → blOrdinal collapse 해시.
     * field = fdId(문자열), value = blOrdinal(문자열).
     * 예: {p}:dc:bl
     */
    static String docCollapseHash(String prefix) {
        return prefix + ":dc:bl";
    }

    /**
     * fdId 오버플로 감지 플래그 키.
     * 예: {p}:dc:overflow
     */
    static String docOverflowFlag(String prefix) {
        return prefix + ":dc:overflow";
    }

    // ── W3 doc-exists (B/L-grain) 버킷 키 생성 메서드 ────────────────────────

    /**
     * W3 doc-exists: status=S인 doc ≥1 보유 B/L의 blOrdinal 비트맵.
     * 예: {p}:bl:dcx:status:CREATED
     */
    static String blDocStatusBitmap(String prefix, String status) {
        return prefix + ":bl:dcx:status:" + status;
    }

    // ── 내부 인코딩 헬퍼 ──────────────────────────────────────────────────────

    /** tax/slip 2-bit 문자열 인코딩: "10" 형식. */
    private static String encodeTs(boolean tax, boolean slip) {
        return (tax ? "1" : "0") + (slip ? "1" : "0");
    }

    /** fdcType → 버킷 세그먼트. null/공백은 "none"으로 정규화. */
    static String encodeType(String fdcType) {
        return (fdcType != null && !fdcType.isBlank()) ? fdcType : "none";
    }
}
