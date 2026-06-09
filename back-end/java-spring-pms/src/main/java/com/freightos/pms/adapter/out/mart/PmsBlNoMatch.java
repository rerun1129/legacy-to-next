package com.freightos.pms.adapter.out.mart;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.regex.Pattern;

/**
 * B/L 번호 MongoDB prefix 정규식 헬퍼.
 *
 * case-sensitive(^앵커만, "i" 플래그 없음)로 생성해야 MongoDB가 인덱스 bounds를 좁힐 수 있다.
 * case-insensitive prefix는 full index scan과 동일하므로 반드시 CS로 유지한다.
 * 입력 값은 Controller→Command 진입 시 이미 대문자 정규화되어 있음이 전제다.
 */
final class PmsBlNoMatch {

    private PmsBlNoMatch() {}

    /**
     * field가 value로 시작하는(prefix) MongoDB Criteria를 반환한다.
     * case-sensitive(^, "i" 없음) — text_pattern_ops 인덱스 bounds 활용.
     */
    static Criteria prefixCriteria(String field, String value) {
        return Criteria.where(field).regex("^" + Pattern.quote(value));
    }
}
