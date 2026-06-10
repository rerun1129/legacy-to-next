package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PmsCountIndexMaintainer 순수 로직 단위 테스트.
 * 라이브 Redis/Mongo 없이 순수 JVM 로직만 검증한다.
 */
class PmsCountIndexMaintainerTest {

    private static final String PREFIX = "pms:ix";

    // ── ordinal 산식 검증 ─────────────────────────────────────────────────────

    @Test
    void HOUSE_blId_N은_ordinal이_N곱하기2이다() {
        assertThat(PmsCountIndexKeys.toOrdinal(1L, "HOUSE")).isEqualTo(2);
        assertThat(PmsCountIndexKeys.toOrdinal(100L, "HOUSE")).isEqualTo(200);
        assertThat(PmsCountIndexKeys.toOrdinal(0L, "HOUSE")).isEqualTo(0);
    }

    @Test
    void MASTER_blId_N은_ordinal이_N곱하기2더하기1이다() {
        assertThat(PmsCountIndexKeys.toOrdinal(1L, "MASTER")).isEqualTo(3);
        assertThat(PmsCountIndexKeys.toOrdinal(100L, "MASTER")).isEqualTo(201);
        assertThat(PmsCountIndexKeys.toOrdinal(0L, "MASTER")).isEqualTo(1);
    }

    @Test
    void HOUSE와_MASTER_같은_blId는_ordinal이_서로다르다() {
        int houseOrd  = PmsCountIndexKeys.toOrdinal(50L, "HOUSE");
        int masterOrd = PmsCountIndexKeys.toOrdinal(50L, "MASTER");
        assertThat(houseOrd).isNotEqualTo(masterOrd);
        // MASTER = HOUSE + 1
        assertThat(masterOrd - houseOrd).isEqualTo(1);
    }

    // ── overflow 가드 검증 ────────────────────────────────────────────────────

    @Test
    void null_blId는_overflow이다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(null)).isTrue();
    }

    @Test
    void 음수_blId는_overflow이다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(-1L)).isTrue();
    }

    @Test
    void 상한_초과_blId는_overflow이다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(PmsCountIndexKeys.ORDINAL_MAX_BL_ID + 1)).isTrue();
    }

    @Test
    void 상한_blId는_overflow아니다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(PmsCountIndexKeys.ORDINAL_MAX_BL_ID)).isFalse();
    }

    @Test
    void blId가_0이면_overflow아니다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(0L)).isFalse();
    }

    // ── deriveMembershipKeys 멤버십 파생 검증 ─────────────────────────────────

    /**
     * W1-A: deriveMembershipKeys는 이제 jobDiv/bound + has-flag + etd/eta만 파생.
     *        cust/spc/liner/pol/pod/salesman/houseteam/salesclass/incoterms dim은 제거됨.
     */
    @Test
    void 차원필드가있는문서는jobDiv_bound_hasflag_etdeta키를포함한다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#100")
            .blId(100L)
            .blType("HOUSE")
            .actualCustomerCode("CUST01")   // Mart 필드는 유지되지만 dim 키 미생성
            .settlePartnerCode("SPC01")
            .linerCode("LINER01")
            .polCode("ICN")
            .podCode("LAX")
            .salesManCode("SM01")
            .houseTeamCode("TEAM01")
            .jobDiv("SEA")
            .bound("EXP")
            .salesClass("SC01")
            .incoterms("FOB")
            .etd("20240101")
            .eta("20240120")
            .hasFreightInput(true)
            .hasTaxIssued(false)
            .hasSlipIssued(false)
            .hasDocumentCreated(true)
            .build();

        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        // W1-A: 잔존 차원 jobDiv/bound + has-flag + ETD/ETA
        assertThat(keys).contains(
            PREFIX + ":bl:jobdiv:SEA",
            PREFIX + ":bl:bound:EXP",
            PREFIX + ":bl:etd:20240101",
            PREFIX + ":bl:eta:20240120",
            PREFIX + ":bl:has:freight",
            PREFIX + ":bl:has:doc"
        );
        // W1-A: 제거된 dim 키들은 생성되지 않아야 함
        assertThat(keys).doesNotContain(
            PREFIX + ":bl:cust:CUST01",
            PREFIX + ":bl:spc:SPC01",
            PREFIX + ":bl:liner:LINER01",
            PREFIX + ":bl:pol:ICN",
            PREFIX + ":bl:pod:LAX",
            PREFIX + ":bl:salesman:SM01",
            PREFIX + ":bl:houseteam:TEAM01",
            PREFIX + ":bl:salesclass:SC01",
            PREFIX + ":bl:incoterms:FOB",
            PREFIX + ":bl:has:tax",
            PREFIX + ":bl:has:slip"
        );
    }

    @Test
    void 차원필드가null인문서는jobDiv_bound없으면_dim키없다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("MASTER#200")
            .blId(200L)
            .blType("MASTER")
            .actualCustomerCode("CUST02")  // W1-A: dim 키 미생성
            .build();

        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        // W1-A: jobDiv/bound null → 차원 키 없음
        assertThat(keys).noneMatch(k -> k.contains(":bl:cust:"));
        assertThat(keys).noneMatch(k -> k.contains(":bl:jobdiv:"));
        assertThat(keys).noneMatch(k -> k.contains(":bl:bound:"));
    }

    @Test
    void etd없는문서는etd버킷키를포함하지않는다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#300")
            .blId(300L)
            .blType("HOUSE")
            .jobDiv("AIR")
            .build();

        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);

        assertThat(keys).noneMatch(k -> k.contains(":bl:etd:"));
        assertThat(keys).noneMatch(k -> k.contains(":bl:eta:"));
    }

    // ── RoaringBitmap 직렬화/역직렬화 검증 ───────────────────────────────────

    @Test
    void serialize_deserialize_왕복이_cardinality를보존한다() {
        RoaringBitmap original = new RoaringBitmap();
        original.add(1);
        original.add(42);
        original.add(999);

        byte[] bytes = PmsCountIndexMaintainer.serialize(original);
        RoaringBitmap restored = PmsCountIndexMaintainer.deserialize(bytes);

        assertThat(restored.getCardinality()).isEqualTo(3);
        assertThat(restored.contains(1)).isTrue();
        assertThat(restored.contains(42)).isTrue();
        assertThat(restored.contains(999)).isTrue();
    }

    @Test
    void 빈byte배열역직렬화는빈비트맵을반환한다() {
        RoaringBitmap bitmap = PmsCountIndexMaintainer.deserialize(new byte[0]);
        assertThat(bitmap.getCardinality()).isEqualTo(0);
    }

    @Test
    void null역직렬화는빈비트맵을반환한다() {
        RoaringBitmap bitmap = PmsCountIndexMaintainer.deserialize(null);
        assertThat(bitmap.getCardinality()).isEqualTo(0);
    }

    // ── OR/AND cardinality 헬퍼 로직 검증 ───────────────────────────────────

    @Test
    void 두비트맵AND교집합cardinality가정확하다() {
        RoaringBitmap a = RoaringBitmap.bitmapOf(1, 2, 3, 4, 5);
        RoaringBitmap b = RoaringBitmap.bitmapOf(3, 4, 5, 6, 7);

        RoaringBitmap intersection = RoaringBitmap.and(a, b);
        assertThat(intersection.getCardinality()).isEqualTo(3); // {3, 4, 5}
    }

    @Test
    void 두비트맵OR합집합cardinality가정확하다() {
        RoaringBitmap a = RoaringBitmap.bitmapOf(1, 2, 3);
        RoaringBitmap b = RoaringBitmap.bitmapOf(3, 4, 5);

        RoaringBitmap union = RoaringBitmap.or(a, b);
        assertThat(union.getCardinality()).isEqualTo(5); // {1, 2, 3, 4, 5}
    }

    // ── applyChanges diff 정확성 (순수 집합 연산으로 검증) ─────────────────────

    @Test
    void old에만있는멤버십은removed이다() {
        // old: {cust:A, cust:B}, new: {cust:A} → removed={cust:B}
        Set<String> oldKeys = Set.of("cust:A", "cust:B");
        Set<String> newKeys = Set.of("cust:A");

        Set<String> removed = new java.util.HashSet<>(oldKeys);
        removed.removeAll(newKeys);

        assertThat(removed).containsExactly("cust:B");
    }

    @Test
    void new에만있는멤버십은added이다() {
        // old: {cust:A}, new: {cust:A, cust:C} → added={cust:C}
        Set<String> oldKeys = Set.of("cust:A");
        Set<String> newKeys = Set.of("cust:A", "cust:C");

        Set<String> added = new java.util.HashSet<>(newKeys);
        added.removeAll(oldKeys);

        assertThat(added).containsExactly("cust:C");
    }

    @Test
    void 양쪽에있는멤버십은변경없다() {
        // old: {cust:A, etd:20240101}, new: {cust:A, etd:20240101}
        Set<String> oldKeys = Set.of("cust:A", "etd:20240101");
        Set<String> newKeys = Set.of("cust:A", "etd:20240101");

        Set<String> added   = new java.util.HashSet<>(newKeys); added.removeAll(oldKeys);
        Set<String> removed = new java.util.HashSet<>(oldKeys); removed.removeAll(newKeys);

        assertThat(added).isEmpty();
        assertThat(removed).isEmpty();
    }
}
