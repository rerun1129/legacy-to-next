package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PmsRedisExactCountProvider 순수 로직 단위 테스트.
 * 지원 형태 판정(isSupportedShape) 메서드를 라이브 Redis/Mongo 없이 검증한다.
 */
class PmsRedisExactCountProviderTest {

    // ── 지원 형태 — null 반환 경로 ─────────────────────────────────────────────

    @Test
    void hblNo필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().hblNoCmd("HBL001").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void mblNo필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().mblNoCmd("MBL001").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void issued필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().issuedCmd("Y").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void documentTypes필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().documentTypesCmd(List.of("INVOICE")).build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void documentStatus필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().documentStatusCmd("ISSUED").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void grouped필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().groupedCmd("Y").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void performanceDtFrom필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().performanceDtFromCmd("20240101").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void documentDtFrom필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().documentDtFromCmd("20240101").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void PERFORMANCE_dateKind에날짜가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", null, "PERFORMANCE", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void taxType필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().taxTypeCmd("VAT").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void financialDocType필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().financialDocTypeCmd("INVOICE").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void documentNoLike필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().documentNoLikeCmd("INV-2024").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    @Test
    void groupFinancialNo필터가있으면미지원형태이다() {
        SearchPmsPerformanceCommand cmd = dimOnlyCmd().groupFinancialNoCmd("GFN-001").build();
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isFalse();
    }

    // ── 지원 형태 — 비-null 경로 ───────────────────────────────────────────────

    @Test
    void 순수차원필터만있으면지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", null, null, null,
            null, null, null, null,
            null, null, "CUST01", "SPC01",
            "LINER01", null, null,
            "SM01", "SC01", "FOB", null, null, "TEAM01", null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    @Test
    void ETD날짜범위와차원필터조합은지원형태이다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, "CUST01", null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
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
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
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
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );
        assertThat(PmsRedisExactCountProvider.isSupportedShape(cmd)).isTrue();
    }

    // ── 테스트용 커맨드 빌더 헬퍼 ─────────────────────────────────────────────

    /** 기본 dim-only 커맨드(line/doc 필터 없음)를 생성한다. */
    private DimCmdBuilder dimOnlyCmd() {
        return new DimCmdBuilder();
    }

    /**
     * 테스트 편의용 커맨드 빌더.
     * 각 필드를 개별 오버라이드해 isSupportedShape 판정 단위 테스트를 단순화한다.
     */
    private static class DimCmdBuilder {
        private String hblNo;
        private String mblNo;
        private String issued;
        private List<String> documentTypes;
        private String documentStatus;
        private String grouped;
        private String performanceDtFrom;
        private String documentDtFrom;
        private String taxType;
        private String financialDocType;
        private String documentNoLike;
        private String groupFinancialNo;

        DimCmdBuilder hblNoCmd(String v) { this.hblNo = v; return this; }
        DimCmdBuilder mblNoCmd(String v) { this.mblNo = v; return this; }
        DimCmdBuilder issuedCmd(String v) { this.issued = v; return this; }
        DimCmdBuilder documentTypesCmd(List<String> v) { this.documentTypes = v; return this; }
        DimCmdBuilder documentStatusCmd(String v) { this.documentStatus = v; return this; }
        DimCmdBuilder groupedCmd(String v) { this.grouped = v; return this; }
        DimCmdBuilder performanceDtFromCmd(String v) { this.performanceDtFrom = v; return this; }
        DimCmdBuilder documentDtFromCmd(String v) { this.documentDtFrom = v; return this; }
        DimCmdBuilder taxTypeCmd(String v) { this.taxType = v; return this; }
        DimCmdBuilder financialDocTypeCmd(String v) { this.financialDocType = v; return this; }
        DimCmdBuilder documentNoLikeCmd(String v) { this.documentNoLike = v; return this; }
        DimCmdBuilder groupFinancialNoCmd(String v) { this.groupFinancialNo = v; return this; }

        SearchPmsPerformanceCommand build() {
            return new SearchPmsPerformanceCommand(
                AggregationBasis.FREIGHT_INPUT, 0, 20,
                "SEA", "EXP", "ETD", "20240101", "20240131",
                performanceDtFrom, null, hblNo, mblNo,
                null, null, null, null,
                null, null, null,
                null, null, null, null, null, null, null,
                documentTypes, documentStatus, documentNoLike,
                documentDtFrom, null, groupFinancialNo,
                grouped, issued, financialDocType, taxType,
                null
            );
        }
    }

}
