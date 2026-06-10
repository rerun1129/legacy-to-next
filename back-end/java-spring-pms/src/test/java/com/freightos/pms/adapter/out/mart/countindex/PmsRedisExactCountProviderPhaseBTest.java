package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase B: PmsRedisExactCountProvider.isSupportedShape Phase A 추가 null 규칙 검증.
 *
 * 기존 PmsRedisExactCountProviderTest 수정 없이 추가 케이스를 별도 클래스로 분리.
 * 라이브 Redis/Mongo 없이 순수 로직만 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 사용한다.
 */
class PmsRedisExactCountProviderPhaseBTest {

    // ── Phase B 추가 null 규칙 ─────────────────────────────────────────────────

    @Test
    void operator_필터가있으면_Phase_A도_미지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, null, "OP001",
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void teamCode있고_DOCUMENT_CREATED이면_미지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.DOCUMENT_CREATED, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, "TEAM01", null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void teamCode있고_FREIGHT_INPUT이면_지원형태이다() {
        // FREIGHT_INPUT에서 teamCode는 houseTeamCode로 B/L-level dim 처리 가능
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, "TEAM01", null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void teamCode있고_TAX_ISSUED이면_지원형태이다() {
        // TAX_ISSUED: teamCode는 houseTeamCode dim으로 B/L-level 처리 가능
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.TAX_ISSUED, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, "TEAM01", null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void 추가_null_규칙없이_기존_지원형태는_여전히_지원형태이다() {
        // 기존 Phase A 지원 케이스: 순수 dim-only
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, "CUST01", null,
            "LINER01", null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }
}
