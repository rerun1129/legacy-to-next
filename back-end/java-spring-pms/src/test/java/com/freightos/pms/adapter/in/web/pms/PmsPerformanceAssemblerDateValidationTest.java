package com.freightos.pms.adapter.in.web.pms;

import com.freightos.pms.adapter.in.web.pms.dto.SearchPmsPerformanceRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PmsPerformanceAssembler.validateDatePair 단위 테스트.
 *
 * W1-B 기간 필수 검증: 세 쌍(ETD/ETA, 실적, 서류) 중 정확히 한 쌍이 양끝 모두 존재해야 한다.
 * 라이브 Spring Context 없이 Assembler 인스턴스만 사용한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
class PmsPerformanceAssemblerDateValidationTest {

    private final PmsPerformanceAssembler assembler = new PmsPerformanceAssembler();

    // ── 0쌍 → 400 ────────────────────────────────────────────────────────────

    @Test
    void 모든_기간_없으면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            null, null, null, null, null, null);
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── 반쪽(한쪽만) → 400 ──────────────────────────────────────────────────

    @Test
    void dateFrom만있고dateTo없으면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            "20240101", null, null, null, null, null);
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dateTo만있고dateFrom없으면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            null, "20240131", null, null, null, null);
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void performanceDtFrom만있고To없으면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            null, null, "20240101", null, null, null);
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void documentDtFrom만있고To없으면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            null, null, null, null, "20240101", null);
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── 두 쌍 동시 → 400 ────────────────────────────────────────────────────

    @Test
    void ETD쌍과_실적쌍_동시이면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            "20240101", "20240131", "20240101", "20240131", null, null);
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ETD쌍과_서류쌍_동시이면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            "20240101", "20240131", null, null, "20240101", "20240131");
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 실적쌍과_서류쌍_동시이면_예외가_발생한다() {
        SearchPmsPerformanceRequest req = buildReq(
            null, null, "20240101", "20240131", "20240101", "20240131");
        assertThatThrownBy(() -> assembler.toCommand(req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── 정상 1쌍 통과 ────────────────────────────────────────────────────────

    @Test
    void ETD_ETD_쌍만있으면_정상_커맨드로_변환된다() {
        SearchPmsPerformanceRequest req = buildReq(
            "20240101", "20240131", null, null, null, null);
        assertThat(assembler.toCommand(req)).isNotNull();
    }

    @Test
    void 실적일자_쌍만있으면_정상_커맨드로_변환된다() {
        SearchPmsPerformanceRequest req = buildReq(
            null, null, "20240101", "20240131", null, null);
        assertThat(assembler.toCommand(req)).isNotNull();
    }

    @Test
    void 서류일자_쌍만있으면_정상_커맨드로_변환된다() {
        SearchPmsPerformanceRequest req = buildReq(
            null, null, null, null, "20240101", "20240131");
        assertThat(assembler.toCommand(req)).isNotNull();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private SearchPmsPerformanceRequest buildReq(
            String dateFrom, String dateTo,
            String perfFrom, String perfTo,
            String docFrom, String docTo) {
        return new SearchPmsPerformanceRequest(
            "FREIGHT_INPUT", 0, 20,
            "SEA", "EXP",
            "ETD", dateFrom, dateTo,
            perfFrom, perfTo,
            docFrom, docTo,
            null, null, null, null,
            null, null
        );
    }
}
