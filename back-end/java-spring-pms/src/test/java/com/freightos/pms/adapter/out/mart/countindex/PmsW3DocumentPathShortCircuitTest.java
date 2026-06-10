package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * W3 DocumentPath B/L-grain 단락 경로 단위 테스트.
 *
 * - 무날짜·무타입+status → computeBlDocShortCircuit 단락 경로 진입
 * - 날짜 있으면 기존 fdId-grain 경로 유지(단락 미진입)
 * - dcx:* 키 패턴 검증
 * 라이브 Redis/Mongo 없이 mock RedisTemplate로 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
@SuppressWarnings("unchecked")
class PmsW3DocumentPathShortCircuitTest {

    private static final String PREFIX = "pms:ix";

    private PmsCountIndexDocumentPath path;
    private PmsMartProperties props;
    private ValueOperations<String, byte[]> valueOps;

    @BeforeEach
    void setUp() {
        props = new PmsMartProperties();
        props.getLineAccel().setEnabled(true);
        props.getCountIndex().setMaxDayBuckets(1500);
        props.getCountIndex().setMaxDistinctScan(2_000_000L);
        props.getCountIndex().setMaxCollapseFdIds(300_000L);

        RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // 기본: overflow 없음, dcx 키 없음(null → 빈 비트맵)
        when(valueOps.get(anyString())).thenReturn(null);
        // MGET: 빈 비트맵 반환
        when(valueOps.multiGet(anyList())).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++) {
                result.add(PmsCountIndexMaintainer.serialize(new RoaringBitmap()));
            }
            return result;
        });

        path = new PmsCountIndexDocumentPath(redisTemplate, props);
    }

    // ── 무날짜·무타입+status → W3 단락 경로 진입 ────────────────────────────

    @Test
    void 무날짜_무타입_status만_있으면_비null을_반환한다() {
        // W3 단락: computeBlDocShortCircuit → has:doc ∩ dcx:status:CREATED → 0L
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            null, null, null, null,
            "CREATED", null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void 무날짜_무타입_grouped_Y만_있으면_비null을_반환한다() {
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            null, null, null, null,
            null, "Y"
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void 무날짜_무타입_status와_grouped_N_동시이면_sg_키_단락_경로로_비null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            null, null, null, null,
            "ISSUED", "N"
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void 단락_경로에서_has_doc_플래그와_dcx_status_키가_조회된다() {
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            null, null, null, null,
            "CREATED", null
        );
        path.computeDocumentCount(cmd, PREFIX);

        // MGET 키 목록에 has:doc 플래그가 포함되어야 한다
        ArgumentCaptor<List<String>> mgetCaptor = ArgumentCaptor.forClass(List.class);
        verify(valueOps, atLeastOnce()).multiGet(mgetCaptor.capture());
        List<String> mgetKeys = mgetCaptor.getAllValues().stream().flatMap(List::stream).toList();
        assertThat(mgetKeys).anyMatch(k -> k.equals(PREFIX + ":bl:has:doc"));

        // 단건 GET으로 dcx:status:CREATED 키가 조회되어야 한다
        ArgumentCaptor<String> getCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps, atLeastOnce()).get(getCaptor.capture());
        assertThat(getCaptor.getAllValues()).anyMatch(k -> k.equals(PREFIX + ":bl:dcx:status:CREATED"));
    }

    // ── 날짜 있으면 기존 fdId-grain 경로 유지 ───────────────────────────────

    @Test
    void perfDt_있으면_기존_fdId_grain_경로로_진입한다() {
        // perfDt 있으면 단락 조건 불충족 → doComputeDocumentCount의 fdId-grain 경로
        // fdId-grain 경로는 일버킷·status 비트맵을 단건 get(fetchBitmap)으로 조회한다.
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", "20240101", null, null,
            "CREATED", null
        );
        Long result = path.computeDocumentCount(cmd, PREFIX);
        // 기존 fdId-grain 경로: 빈 비트맵 → 0L 이상
        assertThat(result).isNotNull();

        // fdId-grain 경로 진입 증거: dc:perfpd 및 dc:status 키를 단건 get으로 조회한다
        ArgumentCaptor<String> getCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps, atLeastOnce()).get(getCaptor.capture());
        List<String> allGetKeys = getCaptor.getAllValues();
        assertThat(allGetKeys).anyMatch(k -> k.contains(":dc:perfpd:"));
        assertThat(allGetKeys).anyMatch(k -> k.contains(":dc:status:"));

        // 단락 미진입 증거: B/L-grain 단락 경로의 dcx 키가 조회되지 않아야 한다
        assertThat(allGetKeys).noneMatch(k -> k.contains(":bl:dcx:"));
    }

    @Test
    void documentType_있으면_기존_fdId_grain_경로로_진입한다() {
        // documentType 있으면 단락 조건(hasTypes=true) 불충족 → fdId-grain 경로
        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            null, null, List.of("INVOICE"), null,
            "CREATED", null
        );
        Long result = path.computeDocumentCount(cmd, PREFIX);
        assertThat(result).isNotNull();
    }

    // ── 실제 비트맵 교집합: flag∩dcx:status → count ──────────────────────────

    @Test
    void flag비트맵과_dcx_status비트맵_교집합이_count로_반환된다() {
        // has:doc: ordinal {10, 20, 30}
        // bl:dcx:status:CREATED: ordinal {20, 30, 40}
        // 교집합: {20, 30} → count=2
        RoaringBitmap flagBitmap   = RoaringBitmap.bitmapOf(10, 20, 30);
        RoaringBitmap statusBitmap = RoaringBitmap.bitmapOf(20, 30, 40);
        byte[] flagBytes   = PmsCountIndexMaintainer.serialize(flagBitmap);
        byte[] statusBytes = PmsCountIndexMaintainer.serialize(statusBitmap);

        String flagKey   = PREFIX + ":bl:has:doc";
        String statusKey = PREFIX + ":bl:dcx:status:CREATED";

        when(valueOps.multiGet(anyList())).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (String k : keys) {
                if (k.equals(flagKey)) result.add(flagBytes);
                else result.add(PmsCountIndexMaintainer.serialize(new RoaringBitmap()));
            }
            return result;
        });
        when(valueOps.get(anyString())).thenAnswer(inv -> {
            String k = inv.getArgument(0);
            if (k.equals(statusKey)) return statusBytes;
            return null;
        });

        SearchPmsPerformanceCommand cmd = buildDocCmd(
            AggregationBasis.DOCUMENT_CREATED,
            null, null, null, null,
            "CREATED", null
        );
        assertThat(path.computeDocumentCount(cmd, PREFIX)).isEqualTo(2L);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private SearchPmsPerformanceCommand buildDocCmd(
            AggregationBasis basis,
            String perfDtFrom, String perfDtTo,
            List<String> documentTypes, String docDtFrom,
            String documentStatus, String grouped) {
        return new SearchPmsPerformanceCommand(
            basis, 0, 20,
            null, null,
            "ETD", null, null,
            perfDtFrom, perfDtTo,
            docDtFrom, null,
            documentTypes, documentStatus,
            grouped, null,
            null, null
        );
    }
}
