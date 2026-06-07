package com.freightos.pms.adapter.out.mart.sync;

/**
 * ETL 쿼리 조립기.
 * fl/dc/spine 본문은 PmsMartEtlSqlBody에 격리(DRY).
 * page CTE만 full/incremental에서 다르다:
 *   - full: keyset(:lastId) + LIMIT(:batch)
 *   - incremental: IN(:ids), LIMIT 없음
 *
 * lineArrays=true이면 fi_lines/dc_docs 배열 포함 변형을 사용(line-accel ON 경로).
 * lineArrays=false이면 기존 상수 그대로 사용(OFF 경로, SQL·문서·동작 완전 동일).
 */
final class PmsMartEtlSql {

    private PmsMartEtlSql() {}

    /**
     * full 배치 쿼리.
     *
     * @param lineArrays line-accel ON이면 true — fi_lines/dc_docs 포함 변형 선택
     */
    static String fullBatchSql(boolean lineArrays) {
        String pageCte = """
            page AS (
                SELECT freight_header_id, bl_type, bl_id
                FROM bms.freight_header
                WHERE freight_header_id > :lastId
                ORDER BY freight_header_id
                LIMIT :batch
            )""";
        return buildQuery(pageCte, lineArrays);
    }

    /**
     * full rebuild 레인지 배치 쿼리.
     * :lastId (keyset 시작값, 첫 호출은 loId - 1), :hiId (레인지 상한 포함), :batch (배치 크기).
     *
     * @param lineArrays line-accel ON이면 true
     */
    static String rangeBatchSql(boolean lineArrays) {
        String pageCte = """
            page AS (
                SELECT freight_header_id, bl_type, bl_id
                FROM bms.freight_header
                WHERE freight_header_id > :lastId AND freight_header_id <= :hiId
                ORDER BY freight_header_id
                LIMIT :batch
            )""";
        return buildQuery(pageCte, lineArrays);
    }

    /**
     * incremental 배치 쿼리. :ids (freight_header_id 목록).
     *
     * @param lineArrays line-accel ON이면 true
     */
    static String incrementalBatchSql(boolean lineArrays) {
        String pageCte = """
            page AS (
                SELECT freight_header_id, bl_type, bl_id
                FROM bms.freight_header
                WHERE freight_header_id IN (:ids)
            )""";
        return buildQuery(pageCte, lineArrays);
    }

    private static String buildQuery(String pageCte, boolean lineArrays) {
        if (lineArrays) {
            return "WITH " + pageCte + ",\n"
                + PmsMartEtlSqlBody.FL_CTE_WITH_LINES + ",\n"
                + PmsMartEtlSqlBody.DC_CTE_WITH_DOCS + "\n"
                + PmsMartEtlSqlBody.OUTER_SELECT_AND_JOINS_WITH_ARRAYS;
        }
        return "WITH " + pageCte + ",\n"
            + PmsMartEtlSqlBody.FL_CTE + ",\n"
            + PmsMartEtlSqlBody.DC_CTE + "\n"
            + PmsMartEtlSqlBody.OUTER_SELECT_AND_JOINS;
    }
}
