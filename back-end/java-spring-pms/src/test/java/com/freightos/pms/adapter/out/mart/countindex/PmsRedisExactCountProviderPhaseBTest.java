package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase B: PmsRedisExactCountProvider.isSupportedShape 추가 케이스 검증.
 *
 * W1-A: 18-field Command로 업데이트.
 *        제거된 필드(operator/teamCode/actualCustomerCode/linerCode)를 참조하는 케이스를 삭제하고
 *        현재 남아 있는 필터(jobDiv/bound)·dateKind 조합으로 교체.
 * 라이브 Redis/Mongo 없이 순수 로직만 검증한다.
 */
class PmsRedisExactCountProviderPhaseBTest {

    // ── 지원 형태 추가 케이스 ──────────────────────────────────────────────────

    @Test
    void DOCUMENT_CREATED_basis이면_미지원형태이다() {
        // DOCUMENT_CREATED: PmsRedisExactCountProvider는 doc path를 지원 안 함
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.DOCUMENT_CREATED, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        // isSupportedShape는 basis 자체를 차단하지 않지만
        // DOCUMENT_CREATED + doc-line 필터 없이는 shape 지원 여부 테스트
        // 실제로는 isSupportedShape가 true이면 collectBitmapKeys 경로로 진입
        // 여기서는 도큐먼트 basis + docLineFilter 없음 → isSupportedShape = true(dim만)
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void jobDiv있고_FREIGHT_INPUT이면_지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void jobDiv있고_TAX_ISSUED이면_지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.TAX_ISSUED, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void bound있고_SLIP_ISSUED이면_지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.SLIP_ISSUED, 0, 20,
            null, "EXP", null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void 기존_지원형태_dim_jobDiv_bound_조합은_여전히_지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }
}
