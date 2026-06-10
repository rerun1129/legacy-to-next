package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.document.DocumentAggEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Phase C: PmsCountIndexDocumentPath·PmsCountIndexDocSupport·PmsCountIndexMaintainer
 * doc-grain 순수 로직 단위 테스트.
 *
 * W1-A: 제거된 필드(partyKind/partyCode/portKind/portCode/teamCode/operator/financialDocType)
 *        참조 케이스를 삭제.
 *        collectDimKeys는 이제 jobDiv/bound만 처리.
 * 라이브 Redis/Mongo 없이 JVM 로직만 검증한다.
 */
class PmsCountIndexDocumentPathTest {

    private static final String PREFIX = "pms:ix";

    // ── PmsCountIndexDocSupport: fdId-grain 비트맵 키 파생 ──────────────────────

    /**
     * W1-A: deriveDocBitmapKeysForDoc는 teamCode/operator dim 키를 생성하지 않음.
     */
    @Test
    void doc_모든_필드_있으면_해당_키들이_파생된다() {
        PmsCountIndexDocSupport support = new PmsCountIndexDocSupport(null, PREFIX);
        PmsBlDocEmbedded d = new PmsBlDocEmbedded(
            10L, "20240115", "20240120", "INVOICE", "ISSUED", true, "TEAM01", "팀01", "OP01",
            null, null
        );
        List<String> keys = support.deriveDocBitmapKeysForDoc(d);
        // 잔존 키
        assertThat(keys).contains(
            PREFIX + ":dc:all",
            PREFIX + ":dc:type:INVOICE",
            PREFIX + ":dc:status:ISSUED",
            PREFIX + ":dc:docdt:20240120",
            PREFIX + ":dc:perfpd:20240115"
        );
        // W1-A: teamCode/operator dim 키, grouped 키는 생성 안 됨
        assertThat(keys).doesNotContain(
            PREFIX + ":dc:team:TEAM01",
            PREFIX + ":dc:op:OP01",
            PREFIX + ":dc:grouped"
        );
    }

    @Test
    void fdId_null이면_deriveDocFdIds에서_생략된다() {
        RedisTemplate<String, byte[]> redisMock = makeMockRedisTemplate(Map.of());
        PmsCountIndexDocSupport support = new PmsCountIndexDocSupport(redisMock, PREFIX);

        PmsBlMartDocument martDoc = buildMartDocWithDocs(List.of(
            new PmsBlDocEmbedded(null, null, null, "INVOICE", null, false, null, null, null, null, null),
            new PmsBlDocEmbedded(5L,   null, null, "DEBIT",   null, false, null, null, null, null, null)
        ));
        RoaringBitmap result = support.deriveDocFdIds(martDoc);
        assertThat(result.getCardinality()).isEqualTo(1);
        assertThat(result.contains(5)).isTrue();
    }

    @Test
    void fdId_음수이면_overflow_플래그가_설정된다() {
        byte[][] capturedValue = {null};
        RedisTemplate<String, byte[]> redisMock = makeMockRedisTemplateWithCapture(capturedValue);
        PmsCountIndexDocSupport support = new PmsCountIndexDocSupport(redisMock, PREFIX);

        PmsBlMartDocument martDoc = buildMartDocWithDocs(List.of(
            new PmsBlDocEmbedded(-1L, null, null, "INVOICE", null, false, null, null, null, null, null)
        ));
        RoaringBitmap result = support.deriveDocFdIds(martDoc);
        assertThat(result.isEmpty()).isTrue();
        // overflow 플래그 set 호출 확인
        assertThat(capturedValue[0]).isNotNull();
    }

    @Test
    void fdId_MAX_INT_초과이면_overflow_처리된다() {
        byte[][] capturedValue = {null};
        RedisTemplate<String, byte[]> redisMock = makeMockRedisTemplateWithCapture(capturedValue);
        PmsCountIndexDocSupport support = new PmsCountIndexDocSupport(redisMock, PREFIX);

        PmsBlMartDocument martDoc = buildMartDocWithDocs(List.of(
            new PmsBlDocEmbedded((long) Integer.MAX_VALUE + 1L, null, null, "INVOICE", null, false, null, null, null, null, null)
        ));
        RoaringBitmap result = support.deriveDocFdIds(martDoc);
        assertThat(result.isEmpty()).isTrue();
        assertThat(capturedValue[0]).isNotNull();
    }

    // ── same-doc 상관: fdId-grain AND 검증 ────────────────────────────────────

    @Test
    void 같은doc에_status_X와_type_Y가_있으면_fdId_AND_매칭_1() {
        // fdId=100: status=X, type=Y (같은 doc) → 두 조건 모두 fdId=100 포함
        RoaringBitmap statusX = RoaringBitmap.bitmapOf(100, 200);
        RoaringBitmap typeY   = RoaringBitmap.bitmapOf(100, 300);
        RoaringBitmap both    = RoaringBitmap.and(statusX, typeY);
        assertThat(both.getCardinality()).isEqualTo(1); // fdId=100만
        assertThat(both.contains(100)).isTrue();
    }

    @Test
    void 다른doc에_status_X와_type_Y가_있으면_fdId_AND_매칭_0() {
        // fdId=100: status=X만, fdId=200: type=Y만 (다른 doc)
        RoaringBitmap statusX = RoaringBitmap.bitmapOf(100);
        RoaringBitmap typeY   = RoaringBitmap.bitmapOf(200);
        RoaringBitmap both    = RoaringBitmap.and(statusX, typeY);
        assertThat(both.getCardinality()).isEqualTo(0);
    }

    // ── collapse dedup: 같은 B/L의 doc 2건 매칭 → B/L 1 ─────────────────────

    @Test
    void 같은_BL의_doc_2건이_매칭되면_blOrdinal_은_1개이다() {
        // fdId 10, 11 모두 blOrdinal=5에 매핑
        RoaringBitmap fdIds = RoaringBitmap.bitmapOf(10, 11);
        // fdId → blOrdinal 매핑: 10→5, 11→5
        Map<Integer, Integer> collapseMap = Map.of(10, 5, 11, 5);
        RoaringBitmap blOrdinals = new RoaringBitmap();
        for (int fdId : fdIds.toArray()) {
            Integer ord = collapseMap.get(fdId);
            if (ord != null) blOrdinals.add(ord);
        }
        assertThat(blOrdinals.getCardinality()).isEqualTo(1);
    }

    // ── PmsCountIndexDocumentPath null 규칙 ───────────────────────────────────

    @Test
    void DOCUMENT_CREATED_아닌_basis이면_null이다() {
        PmsCountIndexDocumentPath path = makeDocumentPath(true);
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null, null, null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    @Test
    void lineAccel_OFF이면_null이다() {
        PmsCountIndexDocumentPath path = makeDocumentPath(false);
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", "20240131", null, null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    @Test
    void open_range_docDtFrom만_있으면_null이다() {
        PmsCountIndexDocumentPath path = makeDocumentPath(true);
        SearchPmsPerformanceCommand cmd = buildDocCmdWithDocDt(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    @Test
    void open_range_docDtTo만_있으면_null이다() {
        PmsCountIndexDocumentPath path = makeDocumentPath(true);
        SearchPmsPerformanceCommand cmd = buildDocCmdWithDocDt(
            AggregationBasis.DOCUMENT_CREATED,
            null, "20240131"
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    @Test
    void open_range_perfDtFrom만_있으면_null이다() {
        PmsCountIndexDocumentPath path = makeDocumentPath(true);
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", null, null, null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    @Test
    void doc_술어와_날짜가_모두_없으면_null이다() {
        PmsCountIndexDocumentPath path = makeDocumentPath(true);
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            null, null, null, null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    @Test
    void 일수초과이면_null이다() {
        PmsMartProperties props = new PmsMartProperties();
        props.getLineAccel().setEnabled(true);
        props.getCountIndex().setMaxDayBuckets(5);
        RedisTemplate<String, byte[]> redisMock = makeMockRedisTemplate(Map.of());
        PmsCountIndexDocumentPath path = new PmsCountIndexDocumentPath(redisMock, props);

        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", "20240108", // 8일 > 5일 제한
            null, null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    // ── jobDiv/bound dim 키 파생 ──────────────────────────────────────────────

    @Test
    void jobDiv있으면_dim_jobDiv_키가_파생된다() {
        List<String> keys = new ArrayList<>();
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", null, null, null, null,
            null, null, null, null,
            null, null, null, null
        );
        PmsCountIndexBitmapKeyCollector.collectDimKeys(cmd, PREFIX, keys);
        assertThat(keys).contains(PREFIX + ":bl:" + PmsCountIndexKeys.DIM_JOBDIV + ":SEA");
    }

    @Test
    void bound있으면_dim_bound_키가_파생된다() {
        List<String> keys = new ArrayList<>();
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, "EXP", null, null, null,
            null, null, null, null,
            null, null, null, null
        );
        PmsCountIndexBitmapKeyCollector.collectDimKeys(cmd, PREFIX, keys);
        assertThat(keys).contains(PREFIX + ":bl:" + PmsCountIndexKeys.DIM_BOUND + ":EXP");
    }

    @Test
    void jobDiv없으면_dim_jobDiv_키가_없다() {
        List<String> keys = new ArrayList<>();
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, "IMP", null, null, null,
            null, null, null, null,
            null, null, null, null
        );
        PmsCountIndexBitmapKeyCollector.collectDimKeys(cmd, PREFIX, keys);
        assertThat(keys).noneMatch(k -> k.contains(":bl:" + PmsCountIndexKeys.DIM_JOBDIV + ":"));
    }

    // ── bl_docteam 키 파생 제거 확인 ─────────────────────────────────────────────

    /**
     * W1-A: blDocteamBitmap이 deriveMembershipKeys에서 제거됨.
     *        documentCreated.teamCode로부터 :bl:docteam: 키 미생성 확인.
     */
    @Test
    void bl_docteam_키는_deriveMembershipKeys에_더이상포함되지않는다() {
        DocumentAggEmbedded dc = new DocumentAggEmbedded(
            null, 0L, null, null, null, null, null, null, null, null, 0L, "TEAMX", null, null
        );
        PmsBlMartDocument doc = PmsBlMartDocument.builder()
            .id("HOUSE#100")
            .blId(100L)
            .blType("HOUSE")
            .hasDocumentCreated(true)
            .documentCreated(dc)
            .build();
        Set<String> keys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, PREFIX);
        // W1-A: blDocteamBitmap 제거됨 → docteam 키 미생성
        assertThat(keys).noneMatch(k -> k.contains(":bl:docteam:"));
        // has:doc 플래그는 여전히 존재
        assertThat(keys).contains(PREFIX + ":bl:has:doc");
    }

    // ── 일버킷 경계 ──────────────────────────────────────────────────────────

    @Test
    void doc_일버킷_범위_정확히_1일이면_키_1개이다() {
        List<String> perfKeys = PmsCountIndexBitmapKeyCollector.etdDayKeys(PREFIX, "20240115", "20240115");
        assertThat(perfKeys).hasSize(1);
        assertThat(perfKeys.get(0)).isEqualTo(PREFIX + ":bl:etd:20240115");
    }

    // ── 결함2 회귀: maxCollapseFdIds 초과 시 collapse 진입 차단 ──────────────

    @Test
    void fdId_cardinality가_maxCollapseFdIds_초과이면_null이다() {
        // 결함2 회귀: 저선택도 술어는 fdId 집합이 수백만이 될 수 있다.
        // maxCollapseFdIds(300,000)를 낮춰 cardinality가 초과하는 상황을 시뮬레이션.
        PmsMartProperties props = new PmsMartProperties();
        props.getLineAccel().setEnabled(true);
        props.getCountIndex().setMaxDayBuckets(1500);
        props.getCountIndex().setMaxDistinctScan(2_000_000L);
        props.getCountIndex().setMaxCollapseFdIds(5L); // 의도적으로 아주 작게 설정

        // dc:status:ISSUED 비트맵에 6개 fdId → cardinality 6 > maxCollapseFdIds 5 → null
        RoaringBitmap statusBitmap = RoaringBitmap.bitmapOf(1, 2, 3, 4, 5, 6);
        byte[] statusBytes = PmsCountIndexMaintainer.serialize(statusBitmap);

        byte[] allBytes    = PmsCountIndexMaintainer.serialize(statusBitmap);
        byte[] perfpdBytes = PmsCountIndexMaintainer.serialize(statusBitmap);
        String allKey      = "pms:ix:dc:all";
        String perfpdKey   = "pms:ix:dc:perfpd:20240101";
        String statusKey   = "pms:ix:dc:status:ISSUED";

        RedisTemplate<String, byte[]> redisMock = makeMockRedisTemplate(
            Map.of(allKey, allBytes, perfpdKey, perfpdBytes, statusKey, statusBytes)
        );
        PmsCountIndexDocumentPath path = new PmsCountIndexDocumentPath(redisMock, props);

        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", "20240101",
            null, "ISSUED"
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNull();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private PmsCountIndexDocumentPath makeDocumentPath(boolean lineAccelEnabled) {
        PmsMartProperties props = new PmsMartProperties();
        props.getLineAccel().setEnabled(lineAccelEnabled);
        props.getCountIndex().setMaxDayBuckets(1500);
        props.getCountIndex().setMaxDistinctScan(2_000_000L);
        RedisTemplate<String, byte[]> redisMock = makeMockRedisTemplate(Map.of());
        return new PmsCountIndexDocumentPath(redisMock, props);
    }

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, byte[]> makeMockRedisTemplate(Map<String, byte[]> store) {
        RedisTemplate<String, byte[]> tpl = mock(RedisTemplate.class);
        ValueOperations<String, byte[]> ops = mock(ValueOperations.class);
        when(tpl.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenAnswer(inv -> store.get(inv.getArgument(0)));
        when(ops.multiGet(anyList())).thenAnswer(inv -> {
            List<String> reqKeys = inv.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (String k : reqKeys) {
                result.add(store.get(k));
            }
            return result;
        });
        return tpl;
    }

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, byte[]> makeMockRedisTemplateWithCapture(byte[][] capturedValue) {
        RedisTemplate<String, byte[]> tpl = mock(RedisTemplate.class);
        ValueOperations<String, byte[]> ops = mock(ValueOperations.class);
        when(tpl.opsForValue()).thenReturn(ops);
        org.mockito.Mockito.doAnswer(inv -> {
            capturedValue[0] = inv.getArgument(1);
            return null;
        }).when(ops).set(anyString(), org.mockito.ArgumentMatchers.any(byte[].class));
        return tpl;
    }

    private PmsBlMartDocument buildMartDocWithDocs(List<PmsBlDocEmbedded> docs) {
        return PmsBlMartDocument.builder()
            .id("HOUSE#100")
            .blId(100L)
            .blType("HOUSE")
            .docs(docs)
            .build();
    }

    /**
     * 16-field Command 헬퍼.
     */
    private SearchPmsPerformanceCommand buildDocCmd(
            AggregationBasis basis,
            String perfDtFrom, String perfDtTo,
            List<String> documentTypes, String documentStatus) {
        return new SearchPmsPerformanceCommand(
            basis, 0, 20,
            null, null, null, null, null,
            perfDtFrom, perfDtTo,
            null, null,
            documentTypes, documentStatus,
            null, null
        );
    }

    private SearchPmsPerformanceCommand buildDocCmdWithDocDt(
            AggregationBasis basis, String docFrom, String docTo) {
        return new SearchPmsPerformanceCommand(
            basis, 0, 20,
            null, null, null, null, null,
            null, null,
            docFrom, docTo,
            null, null,
            null, null
        );
    }
}
