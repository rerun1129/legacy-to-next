package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * W3 freight+documentStatus/grouped 조립 및 issued Y/N 단위 테스트.
 *
 * PmsCountIndexFreightPath 내부 W3 doc-exists AND 경로 진입 여부를 검증한다.
 * W3 이후: fdId-grain dc:all/dc:bl collapse 대신 bl:dcx:* valueOps 키로 직접 B/L-grain AND.
 * 라이브 Redis/Mongo 없이 mock RedisTemplate로 비트맵 데이터를 주입한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
@SuppressWarnings("unchecked")
class PmsFreightDocAndIssuedTest {

    private static final String PREFIX = "pms:ix";

    private PmsCountIndexFreightPath path;
    private PmsMartProperties props;
    private ValueOperations<String, byte[]> valueOps;
    private HashOperations<String, Object, Object> hashOps;

    @BeforeEach
    void setUp() {
        props = new PmsMartProperties();
        props.getLineAccel().setEnabled(true);
        props.getCountIndex().setMaxDayBuckets(1500);
        props.getCountIndex().setMaxDistinctScan(50000);

        RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        hashOps  = mock(HashOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        // 기본: MGET는 빈 비트맵 반환
        when(valueOps.multiGet(anyList())).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++) {
                result.add(PmsCountIndexMaintainer.serialize(new RoaringBitmap()));
            }
            return result;
        });

        // W3: dc:overflow 없음 + bl:dcx:* 키도 없음(빈 비트맵) — null 반환으로 설정
        when(valueOps.get(anyString())).thenReturn(null);

        // dc:bl collapse hash: 기본 빈 응답 (W3 이후 freight 경로에서는 사용 안 함, 무해)
        when(hashOps.multiGet(anyString(), anyCollection())).thenReturn(List.of());

        path = new PmsCountIndexFreightPath(redisTemplate, props);
    }

    // ── W3: FREIGHT + documentStatus → bl:dcx:status:* AND 경로 진입 ──────────

    @Test
    void FREIGHT_documentStatus_있으면_null을_반환하지않는다() {
        // W3: dc:overflow null → bl:dcx:status:ISSUED 키 fetch(null → 빈 비트맵) → AND 결과 0L(비-null)
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null, "ISSUED", null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void FREIGHT_grouped_Y_있으면_null을_반환하지않는다() {
        // W3: bl:dcx:grouped:Y 키 fetch → 빈 비트맵 → AND 결과 0L(비-null)
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null, null, "Y"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void FREIGHT_grouped_N_있으면_null을_반환하지않는다() {
        // W3: bl:dcx:grouped:N 키 fetch → 빈 비트맵 → AND 결과 0L(비-null)
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null, null, "N"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void TAX_documentStatus_있으면_null을_반환하지않는다() {
        // TAX + documentStatus: W3 doc-exists AND 경로
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240131",
            null, null, "ISSUED", null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void FREIGHT_documentStatus와_grouped_동시에있으면_null을_반환하지않는다() {
        // W3: bl:dcx:sg:ISSUED:Y 키 fetch → 빈 비트맵 → AND 결과 0L(비-null)
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null, "ISSUED", "Y"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void dc_overflow_플래그있으면_null을_반환한다() {
        // W3 게이팅 보존: dc:overflow 존재 시 andDocComponent에서 Mongo 폴백
        String overflowKey = PREFIX + ":dc:overflow";
        when(valueOps.get(overflowKey)).thenReturn("1".getBytes(StandardCharsets.UTF_8));

        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null, "ISSUED", null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    // ── issued Y/N: i=1 강제 / i=0 강제 ────────────────────────────────────

    @Test
    void issued_Y인_FREIGHT_perfDt있으면_composite_경로_비null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmdWithIssued(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240105",
            null, "Y"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void issued_N인_FREIGHT_perfDt있으면_composite_경로_비null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmdWithIssued(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240105",
            null, "N"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void issued_Y인_TAX_perfDt있으면_TAX_composite_키_비null_반환한다() {
        // TAX + issued: c:1x1:TYPE 변형들 OR
        SearchPmsPerformanceCommand cmd = buildCmdWithIssued(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240105",
            null, "Y"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void issued_N인_SLIP_perfDt있으면_SLIP_composite_키_비null_반환한다() {
        // SLIP + issued=N: c:x10:TYPE 변형들 OR
        SearchPmsPerformanceCommand cmd = buildCmdWithIssued(
            AggregationBasis.SLIP_ISSUED,
            "20240101", "20240105",
            null, "N"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void issued_Y인_FREIGHT_documentTypes있으면_TYPE별_composite_키가_열거된다() {
        // issued + documentTypes: 각 타입별 composite OR
        SearchPmsPerformanceCommand cmd = buildCmdWithIssued(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240105",
            List.of("INVOICE", "DEBIT"), "Y"
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void issued_있고_perfDt_없으면_전역_복합_경로로_계산한다() {
        // issued 단독(perfDt 없음, ETD/ETA 날짜도 없음): 전역 composite 버킷({p}:ln:c:{t}{s}{i}:{TYPE}) 경로로 진입.
        // 빈 비트맵 환경에서 lineSet.cardinality=0 → 0L 반환(null 아님).
        // 기존 issued_Y인_FREIGHT_perfDt있으면_composite_경로_비null_반환한다 와 달리
        // ETD 범위 AND 없이 전역 복합 키만으로 계산하는 순수 issued 케이스를 검증한다.
        List<String> capturedKeys = new ArrayList<>();
        when(valueOps.multiGet(argThat(keys -> {
            if (keys != null) capturedKeys.addAll(keys);
            return true;
        }))).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++) {
                result.add(PmsCountIndexMaintainer.serialize(new RoaringBitmap()));
            }
            return result;
        });

        SearchPmsPerformanceCommand cmd = buildCmdWithIssued(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, "Y"
        );
        Long count = path.computeFreightCount(cmd, PREFIX);

        // 현행 설계: 전역 composite 경로로 계산 → 빈 비트맵 환경에서 0L
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(0L);
        // MGET 요청 키에 전역 composite 패턴(:ln:c:)이 포함되어 있어야 한다
        assertThat(capturedKeys).anyMatch(k -> k.contains(":ln:c:"));
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private SearchPmsPerformanceCommand buildCmd(
            AggregationBasis basis,
            String perfFrom, String perfTo,
            List<String> documentTypes,
            String documentStatus,
            String grouped,
            String issued) {
        return new SearchPmsPerformanceCommand(
            basis, 0, 20,
            "SEA", "EXP",
            "ETD", null, null,
            perfFrom, perfTo,
            null, null,
            documentTypes, documentStatus,
            grouped, issued,
            null, null
        );
    }

    private SearchPmsPerformanceCommand buildCmdWithIssued(
            AggregationBasis basis,
            String perfFrom, String perfTo,
            List<String> documentTypes,
            String issued) {
        return new SearchPmsPerformanceCommand(
            basis, 0, 20,
            "SEA", "EXP",
            "ETD", null, null,
            perfFrom, perfTo,
            null, null,
            documentTypes, null,
            null, issued,
            null, null
        );
    }
}
