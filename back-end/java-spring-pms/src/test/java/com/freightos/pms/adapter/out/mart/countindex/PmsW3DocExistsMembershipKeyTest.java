package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * W3 B/L-grain doc-exists 비트맵 키 파생 단위 테스트.
 *
 * PmsCountIndexMaintainer.deriveMembershipKeys에서
 * docs[]의 status/grouped 조합으로 dcx:* 키가 올바르게 파생되는지 검증한다.
 * 라이브 Redis/Mongo 없이 순수 JVM 로직만 테스트한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
class PmsW3DocExistsMembershipKeyTest {

    private static final String PREFIX = "pms:ix";

    // ── status 있는 doc → dcx:status + dcx:grouped + dcx:sg 키 파생 ───────────

    @Test
    void status_있는_grouped_false_doc이면_status키와_grouped_N키와_sg키가_파생된다() {
        PmsBlMartDocument doc = buildDocWithDocs(List.of(
            new PmsBlDocEmbedded(10L, null, null, "INVOICE", "CREATED", false, null, null, null, null, null)
        ));
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).contains(PREFIX + ":bl:dcx:status:CREATED");
        assertThat(keys).contains(PREFIX + ":bl:dcx:grouped:N"); // grouped=false → N
        assertThat(keys).contains(PREFIX + ":bl:dcx:sg:CREATED:N");
    }

    @Test
    void status_있는_grouped_true_doc이면_status키와_grouped_Y키와_sg_Y키가_파생된다() {
        PmsBlMartDocument doc = buildDocWithDocs(List.of(
            new PmsBlDocEmbedded(20L, null, null, "DEBIT", "ISSUED", true, null, null, null, null, null)
        ));
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).contains(PREFIX + ":bl:dcx:status:ISSUED");
        assertThat(keys).contains(PREFIX + ":bl:dcx:grouped:Y"); // grouped=true → Y
        assertThat(keys).contains(PREFIX + ":bl:dcx:sg:ISSUED:Y");
    }

    // ── status 없는 doc → dcx:grouped 키만 파생 ─────────────────────────────

    @Test
    void status_없는_grouped_false_doc이면_grouped_N키만_파생된다() {
        PmsBlMartDocument doc = buildDocWithDocs(List.of(
            new PmsBlDocEmbedded(30L, null, null, "INVOICE", null, false, null, null, null, null, null)
        ));
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).contains(PREFIX + ":bl:dcx:grouped:N");
        // status 없으므로 dcx:status 키 미생성
        assertThat(keys).noneMatch(k -> k.contains(":bl:dcx:status:"));
        // sg 키 미생성
        assertThat(keys).noneMatch(k -> k.contains(":bl:dcx:sg:"));
    }

    @Test
    void status_없는_grouped_true_doc이면_grouped_Y키만_파생된다() {
        PmsBlMartDocument doc = buildDocWithDocs(List.of(
            new PmsBlDocEmbedded(40L, null, null, "DEBIT", null, true, null, null, null, null, null)
        ));
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).contains(PREFIX + ":bl:dcx:grouped:Y");
        assertThat(keys).noneMatch(k -> k.contains(":bl:dcx:status:"));
        assertThat(keys).noneMatch(k -> k.contains(":bl:dcx:sg:"));
    }

    // ── docs null/empty이면 dcx:* 키 미파생 ─────────────────────────────────

    @Test
    void docs_null이면_dcx_키가_파생되지_않는다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#1")
            .blId(1L)
            .blType("HOUSE")
            .docs(null)
            .build();
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).noneMatch(k -> k.contains(":bl:dcx:"));
    }

    @Test
    void docs_빈_리스트이면_dcx_키가_파생되지_않는다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#2")
            .blId(2L)
            .blType("HOUSE")
            .docs(List.of())
            .build();
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).noneMatch(k -> k.contains(":bl:dcx:"));
    }

    // ── 여러 docs → 중복 없는 키 집합 파생 ───────────────────────────────────

    @Test
    void 여러_docs에서_status_grouped_조합_키_집합이_파생된다() {
        // doc1: status=CREATED, grouped=false → dcx:status:CREATED, dcx:grouped:N, dcx:sg:CREATED:N
        // doc2: status=ISSUED,  grouped=true  → dcx:status:ISSUED,  dcx:grouped:Y, dcx:sg:ISSUED:Y
        // doc3: status=CREATED, grouped=true  → dcx:status:CREATED(중복), dcx:grouped:Y(중복), dcx:sg:CREATED:Y
        PmsBlMartDocument doc = buildDocWithDocs(List.of(
            new PmsBlDocEmbedded(10L, null, null, "INVOICE", "CREATED", false, null, null, null, null, null),
            new PmsBlDocEmbedded(20L, null, null, "DEBIT",   "ISSUED",  true,  null, null, null, null, null),
            new PmsBlDocEmbedded(30L, null, null, "PAYMENT", "CREATED", true,  null, null, null, null, null)
        ));
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).contains(PREFIX + ":bl:dcx:status:CREATED");
        assertThat(keys).contains(PREFIX + ":bl:dcx:status:ISSUED");
        assertThat(keys).contains(PREFIX + ":bl:dcx:grouped:N");
        assertThat(keys).contains(PREFIX + ":bl:dcx:grouped:Y");
        assertThat(keys).contains(PREFIX + ":bl:dcx:sg:CREATED:N");
        assertThat(keys).contains(PREFIX + ":bl:dcx:sg:ISSUED:Y");
        assertThat(keys).contains(PREFIX + ":bl:dcx:sg:CREATED:Y");
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private PmsBlMartDocument buildDocWithDocs(List<PmsBlDocEmbedded> docs) {
        return PmsBlMartDocument.builder()
            .id("HOUSE#100")
            .blId(100L)
            .blType("HOUSE")
            .docs(docs)
            .build();
    }
}
