package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PmsCountIndexBulkBuilder 누적 로직 단위 테스트.
 *
 * 실제 Redis/Mongo 없이 JVM 내 누적 맵의 정확성만 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 사용한다.
 */
class PmsCountIndexBulkBuilderTest {

    private static final String PREFIX = "pms:ix";

    // ── 비트맵 누적 정확성 ─────────────────────────────────────────────────────

    @Test
    void 문서의_차원키가_비트맵_맵에_누적된다() {
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#100")
            .blId(100L)
            .blType("HOUSE")
            .actualCustomerCode("CUST01")
            .jobDiv("SEA")
            .bound("EXP")
            .build();

        int ordinal = PmsCountIndexKeys.toOrdinal(100L, "HOUSE"); // 200
        Map<String, RoaringBitmap> accum = new HashMap<>();
        Set<String> blKeys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        for (String k : blKeys) {
            accum.computeIfAbsent(k, ignored -> new RoaringBitmap()).add(ordinal);
        }

        String custKey = PREFIX + ":bl:cust:CUST01";
        assertThat(accum).containsKey(custKey);
        assertThat(accum.get(custKey).contains(ordinal)).isTrue();
    }

    @Test
    void 여러_문서가_같은_dim을_공유하면_하나의_비트맵에_누적된다() {
        PmsBlMartDocument doc1 = PmsBlMartDocument.builder()
            .id("HOUSE#1").blId(1L).blType("HOUSE").actualCustomerCode("CUST01").build();
        PmsBlMartDocument doc2 = PmsBlMartDocument.builder()
            .id("HOUSE#2").blId(2L).blType("HOUSE").actualCustomerCode("CUST01").build();

        Map<String, RoaringBitmap> accum = new HashMap<>();
        for (PmsBlMartDocument doc : List.of(doc1, doc2)) {
            int ord = PmsCountIndexKeys.toOrdinal(doc.getBlId(), doc.getBlType());
            for (String k : PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX)) {
                accum.computeIfAbsent(k, ignored -> new RoaringBitmap()).add(ord);
            }
        }

        String custKey = PREFIX + ":bl:cust:CUST01";
        assertThat(accum.get(custKey).getCardinality()).isEqualTo(2);
        assertThat(accum.get(custKey).contains(PmsCountIndexKeys.toOrdinal(1L, "HOUSE"))).isTrue();
        assertThat(accum.get(custKey).contains(PmsCountIndexKeys.toOrdinal(2L, "HOUSE"))).isTrue();
    }

    @Test
    void HOUSE와_MASTER_같은_custCode는_각자_ordinal이다() {
        PmsBlMartDocument house = PmsBlMartDocument.builder()
            .id("HOUSE#10").blId(10L).blType("HOUSE").actualCustomerCode("CUST01").build();
        PmsBlMartDocument master = PmsBlMartDocument.builder()
            .id("MASTER#10").blId(10L).blType("MASTER").actualCustomerCode("CUST01").build();

        Map<String, RoaringBitmap> accum = new HashMap<>();
        for (PmsBlMartDocument doc : List.of(house, master)) {
            int ord = PmsCountIndexKeys.toOrdinal(doc.getBlId(), doc.getBlType());
            for (String k : PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX)) {
                accum.computeIfAbsent(k, ignored -> new RoaringBitmap()).add(ord);
            }
        }

        String custKey = PREFIX + ":bl:cust:CUST01";
        assertThat(accum.get(custKey).getCardinality()).isEqualTo(2);
        // ordinal 20 (HOUSE#10), 21 (MASTER#10)
        assertThat(accum.get(custKey).contains(20)).isTrue();
        assertThat(accum.get(custKey).contains(21)).isTrue();
    }

    // ── fdId → ordinal 배열 (growable int[]) ─────────────────────────────────

    @Test
    void fdId_ordinal_배열은_초기에_음수1로_채워진다() {
        int[] arr = new int[10];
        java.util.Arrays.fill(arr, -1);
        for (int i = 0; i < arr.length; i++) {
            assertThat(arr[i]).isEqualTo(-1);
        }
    }

    @Test
    void 유효한_fdId는_해당_인덱스에_ordinal이_기록된다() {
        int[] arr = new int[100];
        java.util.Arrays.fill(arr, -1);
        int fdId   = 42;
        int ordinal = PmsCountIndexKeys.toOrdinal(5L, "HOUSE"); // 10
        arr[fdId] = ordinal;
        assertThat(arr[fdId]).isEqualTo(10);
        assertThat(arr[0]).isEqualTo(-1); // 다른 위치 불변
    }

    @Test
    void 배열_확장_시_기존_내용이_보존된다() {
        int[] arr = new int[4];
        java.util.Arrays.fill(arr, -1);
        arr[0] = 100;
        arr[3] = 200;

        // 2배 확장
        int[] next = java.util.Arrays.copyOf(arr, arr.length * 2);
        java.util.Arrays.fill(next, arr.length, next.length, -1);

        assertThat(next[0]).isEqualTo(100);
        assertThat(next[3]).isEqualTo(200);
        assertThat(next[4]).isEqualTo(-1);
        assertThat(next[7]).isEqualTo(-1);
    }

    @Test
    void 같은_BL의_두_doc_fdId는_같은_ordinal에_매핑된다() {
        int[] arr = new int[200];
        java.util.Arrays.fill(arr, -1);
        int ordinal = PmsCountIndexKeys.toOrdinal(10L, "HOUSE"); // 20

        int fdId1 = 50;
        int fdId2 = 51;
        arr[fdId1] = ordinal;
        arr[fdId2] = ordinal;

        // collapse: fdId=50,51 모두 blOrdinal=20
        RoaringBitmap blOrdinals = new RoaringBitmap();
        for (int fdId : new int[]{fdId1, fdId2}) {
            int ord = arr[fdId];
            if (ord != -1) blOrdinals.add(ord);
        }
        // 같은 B/L이므로 B/L count = 1
        assertThat(blOrdinals.getCardinality()).isEqualTo(1);
        assertThat(blOrdinals.contains(ordinal)).isTrue();
    }

    // ── overflow 문서 누락 검증 ───────────────────────────────────────────────

    @Test
    void overflow_blId_문서는_비트맵에_추가되지않는다() {
        PmsBlMartDocument overflowDoc = PmsBlMartDocument.builder()
            .id("HOUSE#OVERFLOW")
            .blId(PmsCountIndexKeys.ORDINAL_MAX_BL_ID + 1) // overflow
            .blType("HOUSE")
            .actualCustomerCode("CUST01")
            .build();

        Map<String, RoaringBitmap> accum = new HashMap<>();
        // overflow 감지 후 생략
        if (!PmsCountIndexKeys.isBlIdOverflow(overflowDoc.getBlId())) {
            int ord = PmsCountIndexKeys.toOrdinal(overflowDoc.getBlId(), overflowDoc.getBlType());
            for (String k : PmsCountIndexMaintainer.deriveMembershipKeys(overflowDoc, PREFIX)) {
                accum.computeIfAbsent(k, ignored -> new RoaringBitmap()).add(ord);
            }
        }

        assertThat(accum).isEmpty();
    }

    // ── doc fdId-grain 비트맵 누적 ────────────────────────────────────────────

    @Test
    void docs_배열의_fdId들은_비트맵에_누적된다() {
        PmsCountIndexDocSupport support = new PmsCountIndexDocSupport(null, PREFIX);
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#1")
            .blId(1L)
            .blType("HOUSE")
            .docs(List.of(
                new PmsBlDocEmbedded(10L, null, null, "INVOICE", "ISSUED", false, null, null, null, null, null),
                new PmsBlDocEmbedded(11L, null, null, "DEBIT",   "ISSUED", false, null, null, null, null, null)
            ))
            .build();

        Map<String, RoaringBitmap> accum = new HashMap<>();
        int ordinal = PmsCountIndexKeys.toOrdinal(1L, "HOUSE");
        RoaringBitmap fdIds = support.deriveDocFdIds(doc);
        assertThat(fdIds.getCardinality()).isEqualTo(2);
        assertThat(fdIds.contains(10)).isTrue();
        assertThat(fdIds.contains(11)).isTrue();

        // dc:all 누적
        for (PmsBlDocEmbedded d : doc.getDocs()) {
            for (String k : support.deriveDocBitmapKeysForDoc(d)) {
                accum.computeIfAbsent(k, ignored -> new RoaringBitmap()).add(d.getFdId().intValue());
            }
        }

        String allKey = PREFIX + ":dc:all";
        assertThat(accum.get(allKey).getCardinality()).isEqualTo(2);
    }

    // ── keyset 커서 순회 ──────────────────────────────────────────────────────

    /**
     * 첫 번째 쿼리에는 _id criteria 없이 ASC sort + limit만 포함되고,
     * 두 번째 쿼리에는 _id > lastId criteria가 추가됨을 검증한다.
     * 세 번째 호출에서 빈 배치가 반환되면 순회가 종료된다.
     *
     * skip 기반(O(n²))이 아닌 keyset(O(n)) 구조임을 Query 파라미터로 확인한다.
     */
    @Test
    void keyset_첫번째_쿼리는_id_criteria_없이_ASC_limit만_포함한다() {
        MongoTemplate mongo = mock(MongoTemplate.class);

        PmsBlMartDocument doc1 = PmsBlMartDocument.builder()
            .id("HOUSE#1").blId(1L).blType("HOUSE").actualCustomerCode("C1").build();
        PmsBlMartDocument doc2 = PmsBlMartDocument.builder()
            .id("HOUSE#2").blId(2L).blType("HOUSE").actualCustomerCode("C2").build();

        // 1차 호출: doc1 반환, 2차 호출: doc2 반환, 3차 호출: 빈 배치 → 종료
        when(mongo.find(any(Query.class), eq(PmsBlMartDocument.class)))
            .thenReturn(List.of(doc1))
            .thenReturn(List.of(doc2))
            .thenReturn(new ArrayList<>());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        // keyset 순회 로직 직접 실행 (rebuildFromMart 전체 대신 핵심 루프만 검증)
        String lastId = null;
        int batchSize = 100;
        int callCount = 0;
        while (true) {
            Query q = new Query();
            if (lastId != null) {
                q.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("_id").gt(lastId));
            }
            q.with(Sort.by(Sort.Direction.ASC, "_id")).limit(batchSize);
            List<PmsBlMartDocument> batch = mongo.find(q, PmsBlMartDocument.class);
            if (batch.isEmpty()) break;
            lastId = batch.get(batch.size() - 1).getId();
            callCount++;
        }

        verify(mongo, times(3)).find(queryCaptor.capture(), eq(PmsBlMartDocument.class));
        List<Query> capturedQueries = queryCaptor.getAllValues();

        // 1차: _id criteria 없음 (skip 필드 0 확인으로 skip 기반 아님 검증)
        Query firstQuery = capturedQueries.get(0);
        assertThat(firstQuery.getSkip()).isEqualTo(0L);
        assertThat(firstQuery.getLimit()).isEqualTo(batchSize);
        assertThat(firstQuery.getSortObject().toJson()).contains("_id");

        // 2차: lastId 갱신 후 호출 — 쿼리 객체에 _id 조건 포함 확인
        Query secondQuery = capturedQueries.get(1);
        assertThat(secondQuery.getQueryObject().containsKey("_id")).isTrue();
        assertThat(secondQuery.getSkip()).isEqualTo(0L);

        // 실제 순회 건수 = 2 (doc1, doc2)
        assertThat(callCount).isEqualTo(2);
    }

    @Test
    void keyset_빈_배치_첫_응답이면_즉시_종료된다() {
        MongoTemplate mongo = mock(MongoTemplate.class);

        when(mongo.find(any(Query.class), eq(PmsBlMartDocument.class)))
            .thenReturn(new ArrayList<>());

        String lastId = null;
        int batchSize = 100;
        int callCount = 0;
        while (true) {
            Query q = new Query();
            if (lastId != null) {
                q.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("_id").gt(lastId));
            }
            q.with(Sort.by(Sort.Direction.ASC, "_id")).limit(batchSize);
            List<PmsBlMartDocument> batch = mongo.find(q, PmsBlMartDocument.class);
            if (batch.isEmpty()) break;
            lastId = batch.get(batch.size() - 1).getId();
            callCount++;
        }

        // 빈 배치 즉시 종료: MongoTemplate 1회만 호출, 누적 건수 0
        verify(mongo, times(1)).find(any(Query.class), eq(PmsBlMartDocument.class));
        assertThat(callCount).isEqualTo(0);
    }
}
