package com.freightos.bms.adapter.out.persistence.financialdocument;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * group_no_seq 채번 전용 JDBC 레포지토리.
 *
 * @Modifying + RETURNING 방식은 Hibernate 6.x에서 int 반환값 매핑이
 * 신뢰성 있게 동작하지 않아 JdbcTemplate.queryForObject로 대체한다.
 * UPSERT + RETURNING은 단일 문장의 원자적 실행이므로 동시성 안전(C7).
 */
@Repository
@RequiredArgsConstructor
public class GroupNoSeqJdbcRepository {

    private static final String UPSERT_SQL =
        "INSERT INTO bms.group_no_seq (group_category, yymm, last_seq) " +
        "VALUES (?, ?, 1) " +
        "ON CONFLICT (group_category, yymm) " +
        "DO UPDATE SET last_seq = bms.group_no_seq.last_seq + 1 " +
        "RETURNING last_seq";

    private final JdbcTemplate jdbcTemplate;

    /**
     * 해당 카테고리·월의 다음 시퀀스를 원자적으로 채번한다.
     * INSERT 충돌 시 기존 행의 last_seq를 +1 증가 후 반환.
     */
    public int upsertNextSeq(String groupCategory, String yymm) {
        Integer result = jdbcTemplate.queryForObject(UPSERT_SQL, Integer.class, groupCategory, yymm);
        if (result == null) {
            throw new IllegalStateException(
                "그룹 채번 실패: group_category=" + groupCategory + ", yymm=" + yymm
            );
        }
        return result;
    }
}
