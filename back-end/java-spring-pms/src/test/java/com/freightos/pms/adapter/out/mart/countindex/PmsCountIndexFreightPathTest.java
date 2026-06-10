package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PmsCountIndexFreightPath 순수 로직 단위 테스트.
 *
 * null 규칙, 버킷 키 파생, 일자 열거를 라이브 Redis/Mongo 없이 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
class PmsCountIndexFreightPathTest {

    private static final String PREFIX = "pms:ix";

    private PmsCountIndexFreightPath path;
    private PmsMartProperties props;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        props = new PmsMartProperties();
        props.getLineAccel().setEnabled(true);
        props.getCountIndex().setMaxDayBuckets(1500);

        RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, byte[]> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // 모든 MGET에 빈 비트맵 반환 — cardinality 계산 확인용
        when(valueOps.multiGet(anyList())).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(0);
            List<byte[]> result = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++) {
                result.add(PmsCountIndexMaintainer.serialize(new RoaringBitmap()));
            }
            return result;
        });

        path = new PmsCountIndexFreightPath(redisTemplate, props);
    }

    // ── null 규칙 ─────────────────────────────────────────────────────────────

    @Test
    void TAX_기준에_documentTypes_있으면_null_반환한다() {
        // TAX + documentTypes → needsLineGrainCorrelation → null
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240131",
            List.of("INVOICE"), null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void FREIGHT_기준에_documentTypes_있으면_지원한다() {
        // FREIGHT + documentTypes → fdc 버킷 사용 → non-null
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            List.of("INVOICE"), null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void SLIP_기준에_financialDocType_있으면_null_반환한다() {
        // SLIP + financialDocType → needsLineGrainCorrelation → null
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.SLIP_ISSUED,
            "20240101", "20240131",
            null, "INVOICE",
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void open_range_perfDtFrom만있으면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", null,
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void open_range_perfDtTo만있으면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, "20240131",
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void 일수초과_maxDayBuckets보다크면_null_반환한다() {
        // maxDayBuckets=5로 제한, 7일 범위
        props.getCountIndex().setMaxDayBuckets(5);
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240107",
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void operator_있으면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null,
            null, null, null, null, "OP001", null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void documentDtFrom_있으면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null,
            null, "20240101", null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void DOCUMENT_CREATED_basis는_freight_경로_미해당이다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", "20240131",
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void perfDt없고_타입필터도없으면_null_반환한다() {
        // FREIGHT + perfDt 없음 + 타입 없음 → null
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void documentTypes와_financialDocType_동시존재하면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            List.of("INVOICE"), "DEBIT",
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void lineAccel_OFF이면_null_반환한다() {
        props.getLineAccel().setEnabled(false);
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    // ── 지원 경로 ─────────────────────────────────────────────────────────────

    @Test
    void FREIGHT_perfDt_양쪽있으면_지원하고_비null을반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240105",
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void TAX_basis_perfDt범위_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240131",
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void SLIP_basis_perfDt범위_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.SLIP_ISSUED,
            "20240101", "20240131",
            null, null,
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void perfDt없고_FREIGHT_financialDocType있으면_전역_fdc_경로로_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, "INVOICE",
            null, null, null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    // ── 커맨드 생성 헬퍼 ──────────────────────────────────────────────────────

    private SearchPmsPerformanceCommand buildCmd(
            AggregationBasis basis,
            String perfDtFrom,
            String perfDtTo,
            List<String> documentTypes,
            String financialDocType,
            String documentStatus,
            String documentDtFrom,
            String issued,
            String grouped,
            String operator,
            String taxType,
            String groupFinancialNo) {

        return new SearchPmsPerformanceCommand(
            basis, 0, 20,
            "SEA", "EXP", "ETD", null, null,
            perfDtFrom, perfDtTo, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, null, operator,
            documentTypes, documentStatus, null,
            documentDtFrom, null, groupFinancialNo,
            grouped, issued, financialDocType, taxType,
            null
        );
    }
}
