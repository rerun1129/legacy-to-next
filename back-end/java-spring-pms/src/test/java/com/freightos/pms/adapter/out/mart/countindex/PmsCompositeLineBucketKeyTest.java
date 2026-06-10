package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.adapter.out.mart.document.PmsBlLineEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * W2 E3 복합 일버킷 파생 단위 테스트.
 *
 * {p}:ln:pd:{day}:c:{t}{s}:{TYPE} 복합 키가 올바르게 파생되는지 검증한다.
 * 2-bit(t/s) 인코딩. issued는 라인 데이터 모델에 잔존하나 키 인코딩에서 제외.
 * 라이브 Redis/Mongo 없이 deriveMembershipKeys 순수 로직만 사용한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
class PmsCompositeLineBucketKeyTest {

    private static final String PREFIX = "pms:ix";

    // ── 복합 일버킷: tax+slip+TYPE 조합 ─────────────────────────────────────────

    @Test
    void tax와slip과TYPE이_모두있는_라인은_정확히_1개_복합버킷키를_생성한다() {
        // tax=true, slip=true, fdcType=INVOICE → c:11:INVOICE
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", "INVOICE", null, true, true, true, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:c:11:INVOICE");
        // 기존 단순 버킷도 포함되어야 한다
        assertThat(keys).contains(
            PREFIX + ":ln:pd:20240115:has-freight",
            PREFIX + ":ln:pd:20240115:has-tax",
            PREFIX + ":ln:pd:20240115:has-slip",
            PREFIX + ":ln:pd:20240115:fdc-INVOICE",
            PREFIX + ":ln:fdc:INVOICE"
        );
    }

    @Test
    void tax_slip_모두false이고_fdcType_null인_라인은_c_00_none_복합버킷을_생성한다() {
        // tax=false, slip=false, fdcType=null → c:00:none
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, false, false, true, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:c:00:none");
    }

    @Test
    void tax_slip_모두false인_라인은_c_00_none_복합버킷을_생성한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:c:00:none");
    }

    @Test
    void tax만true인_라인은_c_10_none을_생성한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, true, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:c:10:none");
    }

    @Test
    void slip만true인_라인은_c_01_none을_생성한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", null, null, false, true, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:c:01:none");
    }

    @Test
    void INVOICE_fdcType이있으면_c_TS_INVOICE_복합버킷을_생성한다() {
        // tax=false, slip=false, fdcType=INVOICE → c:00:INVOICE
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", "INVOICE", null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:c:00:INVOICE");
    }

    // ── pd 공백 라인: 전역 복합 버킷 ─────────────────────────────────────────

    @Test
    void pd_공백_라인은_전역_복합버킷을_생성한다() {
        // tax=false, slip=false, fdcType=INVOICE → ln:c:00:INVOICE
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded(null, "INVOICE", null, false, false, true, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:c:00:INVOICE");
        // 일버킷은 없어야 한다
        assertThat(keys).noneMatch(k -> k.contains(":ln:pd:"));
    }

    @Test
    void pd_공백_fdcType도null인_라인은_전역_c_00_none_복합버킷을_생성한다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded(null, null, null, false, false, false, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(PREFIX + ":ln:c:00:none");
    }

    // ── 다중 라인: 같은 pd 다른 조합 ────────────────────────────────────────

    @Test
    void 같은_pd에서_tax라인과_plain라인이_있으면_두_복합버킷이_모두_생성된다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", "INVOICE", null, true, false, false, null, null),
            new PmsBlLineEmbedded("20240115", "INVOICE", null, false, false, true, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        assertThat(keys).contains(
            PREFIX + ":ln:pd:20240115:c:10:INVOICE",
            PREFIX + ":ln:pd:20240115:c:00:INVOICE"
        );
    }

    // ── W2-fix: pd 보유 라인도 전역 복합에 포함 ──────────────────────────────

    @Test
    void pd_있는_라인은_일_복합과_전역_복합_모두_생성한다() {
        // W2-fix: pd 보유 라인이 전역 복합 버킷에서 빠지면
        // ETD 기간 + documentTypes 등 무날짜 라인-술어 쿼리가 0에 가깝게 과소집계됨.
        // pd 보유 라인 → 일 복합(ln:pd:{day}:c:...) 1개 + 전역 복합(ln:c:...) 1개 모두 적재.
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240115", "INVOICE", null, false, false, true, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        // 일 복합 (tax=false, slip=false → :c:00:)
        assertThat(keys).contains(PREFIX + ":ln:pd:20240115:c:00:INVOICE");
        // 전역 복합 — W2-fix 핵심: pd 보유 라인도 전역 복합에 적재
        assertThat(keys).contains(PREFIX + ":ln:c:00:INVOICE");
    }

    @Test
    void pd_있는_라인은_전역_복합_버킷에_포함된다() {
        // ETD 기간 + documentTypes 쿼리가 ln:c:..:.* 버킷을 조회한다.
        // pd 보유 라인이 이 버킷에 없으면 결과가 거의 0으로 나타나는 결함을 검증.
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240120", null, null, false, false, true, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        // tax=false, slip=false, fdcType=null → 전역 복합 ln:c:00:none
        assertThat(keys).contains(PREFIX + ":ln:c:00:none");
    }

    @Test
    void pd_있는_tax_라인은_전역_복합_tax_버킷에_포함된다() {
        PmsBlMartDocument doc = docWithLines(
            new PmsBlLineEmbedded("20240120", "INVOICE", null, true, false, true, null, null)
        );
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        // 일 복합 (tax=true, slip=false → :c:10:)
        assertThat(keys).contains(PREFIX + ":ln:pd:20240120:c:10:INVOICE");
        // 전역 복합
        assertThat(keys).contains(PREFIX + ":ln:c:10:INVOICE");
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private PmsBlMartDocument docWithLines(PmsBlLineEmbedded... lines) {
        return PmsBlMartDocument.builder()
            .id("HOUSE#200")
            .blId(200L)
            .blType("HOUSE")
            .lines(List.of(lines))
            .build();
    }
}
