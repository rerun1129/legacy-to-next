package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PmsRedisExactCountProvider 순수 로직 단위 테스트.
 * 지원 형태 판정(isSupportedShape) 메서드를 라이브 Redis/Mongo 없이 검증한다.
 *
 * W1-A: 18-field Command 생성자로 업데이트.
 *        제거된 필드(hblNo/mblNo/taxType/financialDocType/documentNoLike/groupFinancialNo)
 *        관련 테스트를 신규 null 규칙(isDocLineFilter / performanceDt / documentDt / PERFORMANCE dateKind)으로 교체.
 */
class PmsRedisExactCountProviderTest {

    // ── 지원 형태 — null 반환 경로 ─────────────────────────────────────────────

    @Test
    void issued필터가있으면미지원형태이다() {
        // issued → hasDocLineFilter=true → false
        SearchPmsPerformanceCommand cmd = buildCmd()
            .issued("Y").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void documentTypes필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = buildCmd()
            .documentTypes(List.of("INVOICE")).build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void documentStatus필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = buildCmd()
            .documentStatus("ISSUED").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void grouped필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = buildCmd()
            .grouped("Y").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void performanceDtFrom필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = buildCmd()
            .performanceDtFrom("20240101").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void documentDtFrom필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = buildCmd()
            .documentDtFrom("20240101").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void PERFORMANCE_dateKind에날짜가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", null, "PERFORMANCE", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    // ── 지원 형태 — 비-null 경로 ───────────────────────────────────────────────

    @Test
    void 순수차원필터만있으면지원형태이다() {
        // jobDiv/bound만 있는 경우 — hasDocLineFilter=false, 날짜 없음
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP",
            null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void ETD날짜범위와차원필터조합은지원형태이다() {
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
    void ETA날짜범위만있는경우지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, "ETA", "20240201", "20240229",
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void 필터없는전체조회는지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    // ── 테스트용 커맨드 빌더 헬퍼 ─────────────────────────────────────────────

    private DimCmdBuilder buildCmd() {
        return new DimCmdBuilder();
    }

    private static class DimCmdBuilder {
        private String issued;
        private List<String> documentTypes;
        private String documentStatus;
        private String grouped;
        private String performanceDtFrom;
        private String documentDtFrom;

        DimCmdBuilder issued(String v)                   { this.issued = v; return this; }
        DimCmdBuilder documentTypes(List<String> v)      { this.documentTypes = v; return this; }
        DimCmdBuilder documentStatus(String v)           { this.documentStatus = v; return this; }
        DimCmdBuilder grouped(String v)                  { this.grouped = v; return this; }
        DimCmdBuilder performanceDtFrom(String v)        { this.performanceDtFrom = v; return this; }
        DimCmdBuilder documentDtFrom(String v)           { this.documentDtFrom = v; return this; }

        SearchPmsPerformanceCommand build() {
            return new SearchPmsPerformanceCommand(
                AggregationBasis.FREIGHT_INPUT, 0, 20,
                "SEA", "EXP", "ETD", "20240101", "20240131",
                performanceDtFrom, null,
                documentDtFrom, null,
                documentTypes, documentStatus,
                grouped, issued,
                null, null
            );
        }
    }
}
