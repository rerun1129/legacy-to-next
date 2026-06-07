package com.freightos.pms.adapter.out.mart.sync;

/**
 * ETL 쿼리 조립기.
 * fl/dc/spine 본문은 PmsMartEtlSqlBody에 격리(DRY).
 * page CTE만 full/incremental에서 다르다:
 *   - full: keyset(:lastId) + LIMIT(:batch)
 *   - incremental: IN(:ids), LIMIT 없음
 */
final class PmsMartEtlSql {

    private PmsMartEtlSql() {}

    /** full 배치 쿼리. :lastId (keyset 시작값 0부터), :batch (배치 크기). */
    static String fullBatchSql() {
        String pageCte = """
            page AS (
                SELECT freight_header_id, bl_type, bl_id
                FROM bms.freight_header
                WHERE freight_header_id > :lastId
                ORDER BY freight_header_id
                LIMIT :batch
            )""";
        return buildQuery(pageCte);
    }

    /**
     * full rebuild 레인지 배치 쿼리.
     * :lastId (keyset 시작값, 첫 호출은 loId - 1), :hiId (레인지 상한 포함), :batch (배치 크기).
     */
    static String rangeBatchSql() {
        String pageCte = """
            page AS (
                SELECT freight_header_id, bl_type, bl_id
                FROM bms.freight_header
                WHERE freight_header_id > :lastId AND freight_header_id <= :hiId
                ORDER BY freight_header_id
                LIMIT :batch
            )""";
        return buildQuery(pageCte);
    }

    /** incremental 배치 쿼리. :ids (freight_header_id 목록). */
    static String incrementalBatchSql() {
        String pageCte = """
            page AS (
                SELECT freight_header_id, bl_type, bl_id
                FROM bms.freight_header
                WHERE freight_header_id IN (:ids)
            )""";
        return buildQuery(pageCte);
    }

    private static String buildQuery(String pageCte) {
        return "WITH " + pageCte + ",\n"
            + PmsMartEtlSqlBody.FL_CTE + ",\n"
            + PmsMartEtlSqlBody.DC_CTE + "\n"
            + PmsMartEtlSqlBody.OUTER_SELECT_AND_JOINS;
    }
}
