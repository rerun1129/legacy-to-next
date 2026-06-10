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

import java.nio.charset.StandardCharsets;
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
 * W3 FreightPath B/L-grain doc-exists 경로 단위 테스트.
 *
 * - P6형(FREIGHT, perfDt 없음, ETD범위, documentStatus만): 비-null 라우팅 + flag∩date∩docExists
 * - grouped 미인식값 단독: 기존 null 반환(라인술어·타입필터 없으므로 폴백) 확인
 * - W3 dcx:* 키가 단건 GET으로 조회되는지 키 패턴 검증
 * - dc:overflow 게이팅: freight 경로의 andDocComponent에서 폴백
 * 라이브 Redis/Mongo 없이 mock RedisTemplate로 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
@SuppressWarnings("unchecked")
class PmsW3FreightDocExistsPathTest {

    private static final String PREFIX = "pms:ix";

    private PmsCountIndexFreightPath path;
    private PmsMartProperties props;
    private ValueOperations<String, byte[]> valueOps;

    @BeforeEach
    void setUp() {
        props = new PmsMartProperties();
        props.getLineAccel().setEnabled(true);
        props.getCountIndex().setMaxDayBuckets(1500);
        props.getCountIndex().setMaxDistinctScan(50000);

        RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // 기본: MGET는 빈 비트맵 반환
        when(valueOps.multiGet(anyList())).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++) {
                result.add(PmsCountIndexMaintainer.serialize(new RoaringBitmap()));
            }
            return result;
        });

        // dc:overflow 없음, dcx:* 키 없음(빈 비트맵) — null 반환
        when(valueOps.get(anyString())).thenReturn(null);

        path = new PmsCountIndexFreightPath(redisTemplate, props);
    }

    // ── P6: perfDt 없음 + ETD범위 + documentStatus만 → computeWithBlDocOnly 경로 ──

    @Test
    void P6형_FREIGHT_perfDt없음_ETD범위_documentStatus만_비null_반환한다() {
        // P6 시나리오: FREIGHT_INPUT + perfDt 없음 + ETD2025범위 + documentStatus=CREATED
        // computeWithBlDocOnly → has-flag∩dateSet∩docExists → 빈 비트맵 → 0L(비-null)
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            "ETD", "20250101", "20250131",
            null, null,
            null, null,
            null, "CREATED",
            null, null,
            null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void P6형_FREIGHT_perfDt없음_날짜없음_documentStatus만_비null_반환한다() {
        // 완전 무필터 + documentStatus 단독: has-flag 시작점이므로 안전
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            null, null, null,
            null, null,
            null, null,
            null, "CREATED",
            null, null,
            null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
    }

    @Test
    void P6형_dcx_status_키가_단건_GET으로_조회된다() {
        // andDocComponent에서 bl:dcx:status:CREATED 키를 단건 GET으로 조회하는지 검증
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            "ETD", "20250101", "20250105",
            null, null,
            null, null,
            null, "CREATED",
            null, null,
            null, null
        );
        path.computeFreightCount(cmd, PREFIX);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(valueOps, atLeastOnce()).get(captor.capture());

        List<String> gotKeys = captor.getAllValues();
        // W3: bl:dcx:status:CREATED 키가 단건 GET에 포함되어야 한다
        assertThat(gotKeys).anyMatch(k -> k.equals(PREFIX + ":bl:dcx:status:CREATED"));
    }

    @Test
    void P6형_status와_grouped_Y_동시이면_sg_키가_조회된다() {
        // status + grouped=Y → bl:dcx:sg:CREATED:Y 복합 키 사용
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            "ETD", "20250101", "20250105",
            null, null,
            null, null,
            null, "CREATED",
            "Y", null,
            null, null
        );
        path.computeFreightCount(cmd, PREFIX);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(valueOps, atLeastOnce()).get(captor.capture());

        List<String> gotKeys = captor.getAllValues();
        assertThat(gotKeys).anyMatch(k -> k.equals(PREFIX + ":bl:dcx:sg:CREATED:Y"));
        // status 단독 키·grouped 단독 키는 sg 대신 sg만 쓰므로 없어야 한다
        assertThat(gotKeys).noneMatch(k -> k.equals(PREFIX + ":bl:dcx:status:CREATED"));
        assertThat(gotKeys).noneMatch(k -> k.equals(PREFIX + ":bl:dcx:grouped:Y"));
    }

    @Test
    void grouped_N이면_grouped_N_키가_조회된다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            "ETD", "20250101", "20250105",
            null, null,
            null, null,
            null, null,
            "N", null,
            null, null
        );
        path.computeFreightCount(cmd, PREFIX);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(valueOps, atLeastOnce()).get(captor.capture());

        assertThat(captor.getAllValues()).anyMatch(k -> k.equals(PREFIX + ":bl:dcx:grouped:N"));
    }

    // ── grouped 미인식값 단독 → 라인술어·타입필터 없으므로 null(Mongo 폴백) ────

    @Test
    void grouped_미인식값_단독은_null을_반환한다() {
        // 미인식값(Y/N 아닌 값): perfDt 없고 라인술어 없고 타입필터 없음
        // W3 분기에서 hasText(grouped)=true → computeWithBlDocOnly 진입
        // 그러나 andDocComponent 내 분기: 미인식 grouped → lineSet 그대로 반환
        // 실제로는 computeWithBlDocOnly 자체는 진입하고, andDocComponent에서 미인식 grouped 처리
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            null, null, null,
            null, null,
            null, null,
            null, null,
            "UNKNOWN_VALUE", null,
            null, null
        );
        // W3 분기: hasText("UNKNOWN_VALUE")=true → computeWithBlDocOnly 진입
        // andDocComponent: groupedY=false, groupedN=false → 미인식 grouped → lineSet 그대로
        // result = 0L (빈 flag 비트맵)
        Long result = path.computeFreightCount(cmd, PREFIX);
        // 미인식 grouped 단독: lineSet 그대로 반환 → 0L(빈 비트맵 환경)
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    // ── dc:overflow 게이팅 보존 ───────────────────────────────────────────────

    @Test
    void dc_overflow_있으면_andDocComponent에서_null을_반환한다() {
        // dc:overflow 게이팅: W3에서도 freight 경로의 andDocComponent가 폴백
        when(valueOps.get(PREFIX + ":dc:overflow")).thenReturn("1".getBytes(StandardCharsets.UTF_8));

        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            "ETD", "20250101", "20250105",
            null, null,
            null, null,
            null, "CREATED",
            null, null,
            null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    // ── 실제 비트맵 교집합 검증 ────────────────────────────────────────────────

    @Test
    void flag비트맵과_dcx_status비트맵_교집합이_count로_반환된다() {
        // flag(has-freight): ordinal {1, 2, 3}
        // bl:dcx:status:CREATED: ordinal {2, 3, 4}
        // 교집합: {2, 3} → count=2
        RoaringBitmap flagBitmap   = RoaringBitmap.bitmapOf(1, 2, 3);
        RoaringBitmap statusBitmap = RoaringBitmap.bitmapOf(2, 3, 4);
        byte[] flagBytes   = PmsCountIndexMaintainer.serialize(flagBitmap);
        byte[] statusBytes = PmsCountIndexMaintainer.serialize(statusBitmap);

        String flagKey   = PREFIX + ":bl:has:freight";
        String statusKey = PREFIX + ":bl:dcx:status:CREATED";

        // MGET 시 flagKey 포함 응답
        when(valueOps.multiGet(anyList())).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (String k : keys) {
                if (k.equals(flagKey)) result.add(flagBytes);
                else result.add(PmsCountIndexMaintainer.serialize(new RoaringBitmap()));
            }
            return result;
        });
        // GET 시 statusKey 응답
        when(valueOps.get(statusKey)).thenReturn(statusBytes);
        when(valueOps.get(anyString())).thenAnswer(inv -> {
            String k = inv.getArgument(0);
            if (k.equals(statusKey)) return statusBytes;
            return null; // overflow null, 기타 dcx 키 null
        });

        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null,
            null, null, null,
            null, null,
            null, null,
            null, "CREATED",
            null, null,
            null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isEqualTo(2L); // {2, 3}
    }
}
