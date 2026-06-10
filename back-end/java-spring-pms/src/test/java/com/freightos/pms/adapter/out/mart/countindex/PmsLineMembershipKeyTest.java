package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.adapter.out.mart.document.PmsBlLineEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase B line(perfdt) 버킷 키 파생 단위 테스트.
 *
 * 라이브 Redis/Mongo 없이 deriveMembershipKeys 순수 로직만 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 사용한다.
 */
class PmsLineMembershipKeyTest {

    private static final String PREFIX = "pms:ix";

    // ── pd 비공백 라인 기본 케이스 ──────────────────────────────────────────────

    @Test
    void pd_비공백_라인은_has_freight_일버킷을_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:has-freight");
    }

    @Test
    void pd_비공백_tax라인은_has_tax_일버킷을_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, true, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(
            PREFIX + ":ln:pd:20240115:has-freight",
            PREFIX + ":ln:pd:20240115:has-tax"
        );
        assertThat(keys).doesNotContain(PREFIX + ":ln:pd:20240115:has-slip");
    }

    @Test
    void pd_비공백_slip라인은_has_slip_일버킷을_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, false, true, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(
            PREFIX + ":ln:pd:20240115:has-freight",
            PREFIX + ":ln:pd:20240115:has-slip"
        );
        assertThat(keys).doesNotContain(PREFIX + ":ln:pd:20240115:has-tax");
    }

    @Test
    void pd_비공백_tax와_slip_동시인_라인은_두_속성_모두_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, true, true, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(
            PREFIX + ":ln:pd:20240115:has-freight",
            PREFIX + ":ln:pd:20240115:has-tax",
            PREFIX + ":ln:pd:20240115:has-slip"
        );
    }

    @Test
    void pd_비공백_fdcType있는_라인은_일버킷과_전역버킷_모두_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", "INVOICE", null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(
            PREFIX + ":ln:pd:20240115:has-freight",
            PREFIX + ":ln:pd:20240115:fdc-INVOICE",
            PREFIX + ":ln:fdc:INVOICE"
        );
    }

    // ── pd 공백 라인 ─────────────────────────────────────────────────────────

    @Test
    void pd_공백_라인은_전역_fdc만_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded(null, "DEBIT", null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:fdc:DEBIT");
        // 일버킷은 포함되지 않아야 함
        assertThat(keys).noneMatch(k -> k.contains(":ln:pd:"));
    }

    @Test
    void pd_빈문자열_라인은_전역_fdc만_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("", "PAYMENT", null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:fdc:PAYMENT");
        assertThat(keys).noneMatch(k -> k.contains(":ln:pd:"));
    }

    @Test
    void pd_공백_fdcType도_null인_라인은_line_키를_추가하지않는다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded(null, null, null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).noneMatch(k -> k.startsWith(PREFIX + ":ln:"));
    }

    // ── lines null/empty ──────────────────────────────────────────────────────

    @Test
    void lines_null인_문서는_line_키를_추가하지않는다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#999")
            .blId(999L)
            .blType("HOUSE")
            .lines(null)
            .build();
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).noneMatch(k -> k.startsWith(PREFIX + ":ln:"));
    }

    @Test
    void lines_빈리스트인_문서는_line_키를_추가하지않는다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#998")
            .blId(998L)
            .blType("HOUSE")
            .lines(List.of())
            .build();
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).noneMatch(k -> k.startsWith(PREFIX + ":ln:"));
    }

    // ── 다중 타입·다중 라인 ──────────────────────────────────────────────────

    @Test
    void 여러_타입의_라인은_각각의_전역_fdc_버킷을_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", "INVOICE", null, false, false, false, null, null),
            new PmsBlLineEmbedded("20240116", "DEBIT", null, false, false, false, null, null),
            new PmsBlLineEmbedded("20240116", "PAYMENT", null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(
            PREFIX + ":ln:fdc:INVOICE",
            PREFIX + ":ln:fdc:DEBIT",
            PREFIX + ":ln:fdc:PAYMENT"
        );
    }

    @Test
    void 같은_pd_다른_타입의_라인은_두_일버킷_모두_포함한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", "INVOICE", null, false, false, false, null, null),
            new PmsBlLineEmbedded("20240115", "CREDIT", null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(
            PREFIX + ":ln:pd:20240115:fdc-INVOICE",
            PREFIX + ":ln:pd:20240115:fdc-CREDIT"
        );
    }

    // ── 일자 열거 (PmsCountIndexBitmapKeyCollector를 통해 간접 검증) ──────────

    @Test
    void 단일_일자_범위는_해당_일만_포함한다() {
        List<String> keys = PmsCountIndexBitmapKeyCollector.etdDayKeys(PREFIX, "20240115", "20240115");
        assertThat(keys).containsExactly(PREFIX + ":bl:etd:20240115");
    }

    @Test
    void 월_경계를_넘는_범위는_올바르게_열거한다() {
        // 1월31일 → 2월1일
        List<String> keys = PmsCountIndexBitmapKeyCollector.etdDayKeys(PREFIX, "20240131", "20240201");
        assertThat(keys).containsExactly(
            PREFIX + ":bl:etd:20240131",
            PREFIX + ":bl:etd:20240201"
        );
    }

    @Test
    void 연_경계를_넘는_범위는_올바르게_열거한다() {
        // 12월31일 → 1월1일
        List<String> keys = PmsCountIndexBitmapKeyCollector.etdDayKeys(PREFIX, "20231231", "20240101");
        assertThat(keys).containsExactly(
            PREFIX + ":bl:etd:20231231",
            PREFIX + ":bl:etd:20240101"
        );
    }

    @Test
    void ETA_일자_범위는_eta_키를_생성한다() {
        List<String> keys = PmsCountIndexBitmapKeyCollector.etaDayKeys(PREFIX, "20240201", "20240203");
        assertThat(keys).containsExactly(
            PREFIX + ":bl:eta:20240201",
            PREFIX + ":bl:eta:20240202",
            PREFIX + ":bl:eta:20240203"
        );
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private PmsBlMartDocument docWithLines(PmsBlLineEmbedded... lines) {
        return PmsBlMartDocument.builder()
            .id("HOUSE#100")
            .blId(100L)
            .blType("HOUSE")
            .lines(List.of(lines))
            .build();
    }
}
