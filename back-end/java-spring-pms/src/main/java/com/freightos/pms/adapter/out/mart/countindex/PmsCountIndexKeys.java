package com.freightos.pms.adapter.out.mart.countindex;

/**
 * Redis Count Index 키 스키마 헬퍼.
 *
 * prefix는 호출측(PmsCountIndexMaintainer, PmsRedisExactCountProvider)에서 주입한다.
 * 모든 메서드는 순수 문자열 조합이므로 Spring 빈이 아니다.
 *
 * 키 레이아웃:
 * - {p}:bl:{dim}:{code}       — dim별 코드 비트맵 (byte[])
 * - {p}:bl:has:{flag}         — basis 존재 플래그 비트맵 (byte[])
 * - {p}:bl:etd:{date}         — ETD 일별 버킷 비트맵 (byte[])
 * - {p}:bl:eta:{date}         — ETA 일별 버킷 비트맵 (byte[])
 * - {p}:ln:pd:{day}:{attr}    — line(perfdt) 일·속성 버킷 비트맵 (byte[]). attr = has-freight|has-tax|has-slip|fdc-{TYPE}
 * - {p}:ln:fdc:{TYPE}         — line 전역 서류타입 버킷 비트맵 (byte[])
 * - {p}:meta                  — 메타 해시 (complete, syncAt)
 * - {p}:bl:overflow           — B/L ordinal overflow 플래그 (존재 시 not-ready)
 */
final class PmsCountIndexKeys {

    // ── dim 코드 상수 ────────────────────────────────────────────────────────

    static final String DIM_CUST        = "cust";
    static final String DIM_SPC         = "spc";
    static final String DIM_LINER       = "liner";
    static final String DIM_POL         = "pol";
    static final String DIM_POD         = "pod";
    static final String DIM_SALESMAN    = "salesman";
    static final String DIM_HOUSETEAM   = "houseteam";
    static final String DIM_JOBDIV      = "jobdiv";
    static final String DIM_BOUND       = "bound";
    static final String DIM_SALESCLASS  = "salesclass";
    static final String DIM_INCOTERMS   = "incoterms";

    // ── has-flag 상수 ────────────────────────────────────────────────────────

    static final String FLAG_FREIGHT = "freight";
    static final String FLAG_TAX     = "tax";
    static final String FLAG_SLIP    = "slip";
    static final String FLAG_DOC     = "doc";

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
     * doc grouped=true 비트맵.
     * 예: {p}:dc:grouped
     */
    static String docGroupedBitmap(String prefix) {
        return prefix + ":dc:grouped";
    }

    /**
     * doc team 비트맵(docs[] 원소 레벨).
     * 예: {p}:dc:team:TEAMCODE
     */
    static String docTeamBitmap(String prefix, String team) {
        return prefix + ":dc:team:" + team;
    }

    /**
     * doc operator 비트맵(docs[] 원소 레벨).
     * 예: {p}:dc:op:OP001
     */
    static String docOperatorBitmap(String prefix, String operator) {
        return prefix + ":dc:op:" + operator;
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

    /**
     * B/L 레벨 docteam 비트맵 (documentCreated.teamCode — fast-path 용, 멤버: blOrdinal).
     * 예: {p}:bl:docteam:TEAMCODE
     */
    static String blDocteamBitmap(String prefix, String teamCode) {
        return prefix + ":bl:docteam:" + teamCode;
    }
}
