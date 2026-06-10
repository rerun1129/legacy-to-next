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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PmsCountIndexFreightPath 순수 로직 단위 테스트.
 *
 * null 규칙, 버킷 키 파생, 일자 열거를 라이브 Redis/Mongo 없이 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 *
 * E3 provider 배선 변경 반영:
 * - TAX/SLIP+types+perfDt → E3 composite 일버킷 경로(비-null).
 * - TAX/SLIP+types+ETD만(perfDt 없음) → 전역 composite 경로(비-null).
 * - ETD+issued Y/N+perfDt 없음 → 전역 composite(i 고정, 비-null).
 * - FREIGHT+issued+perfDt 없음 → 전역 composite(변형 수 가드 포함).
 * - 단일술어 FREIGHT+perfDt, types 없음 → 단순 has-freight 버킷(비-null, 키 패턴 검증).
 * - documentStatus+perfDt → E2 doc AND 경로(비-null/0L, 빈 비트맵 반환 환경).
 */
class PmsCountIndexFreightPathTest {

    private static final String PREFIX = "pms:ix";

    private PmsCountIndexFreightPath path;
    private PmsMartProperties props;
    @SuppressWarnings("unchecked")
    private ValueOperations<String, byte[]> valueOps;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        props = new PmsMartProperties();
        props.getLineAccel().setEnabled(true);
        props.getCountIndex().setMaxDayBuckets(1500);

        RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
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
    void open_range_perfDtFrom만있으면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", null,
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void open_range_perfDtTo만있으면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, "20240131",
            null, null, null, null, null
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
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void documentDtFrom_있으면_null_반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, "20240101", null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void DOCUMENT_CREATED_basis는_freight_경로_미해당이다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.DOCUMENT_CREATED,
            "20240101", "20240131",
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void perfDt없고_라인술어도없고_타입필터도없으면_null_반환한다() {
        // FREIGHT + perfDt 없음 + 타입 없음 + issued 없음 → null
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void lineAccel_OFF이면_null_반환한다() {
        props.getLineAccel().setEnabled(false);
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    // ── E3: TAX/SLIP + types + perfDt → composite 일버킷 경로 ─────────────────

    @Test
    void TAX_types_perfDt범위_E3_composite_일버킷_경로로_지원한다() {
        // E3: TAX+types+perfDt → needsLineGrainCorrelation null 규칙 제거
        //     composite 일버킷 4변형/일×|types| → 빈 비트맵 반환 → 0L
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240103",
            List.of("INVOICE"),
            null, null, null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L); // 빈 비트맵 → cardinality 0
    }

    @Test
    void SLIP_types_perfDt범위_E3_composite_일버킷_경로로_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.SLIP_ISSUED,
            "20240101", "20240103",
            List.of("PAYMENT"),
            null, null, null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void TAX_types_perfDt_composite_키수는_4변형곱일수곱타입수이다() {
        // TAX: t=1 고정, s∈{0,1}, i∈{issued} × 1타입 × 3일 = 2키×3일 = 6키
        // multiGet에 전달된 키 목록에서 composite 키 패턴 확인
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240103",
            List.of("INVOICE"),
            null, null, "Y", null
        );
        path.computeFreightCount(cmd, PREFIX);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(valueOps, atLeastOnce()).multiGet(captor.capture());

        List<String> allKeys = captor.getAllValues().stream()
            .flatMap(List::stream).toList();
        // composite 일버킷 패턴: {p}:ln:pd:{day}:c:{t}{s}{i}:{TYPE}
        long compositeKeyCount = allKeys.stream()
            .filter(k -> k.contains(":ln:pd:") && k.contains(":c:"))
            .count();
        // TAX+issued Y+INVOICE: t=1, s∈{0,1}, i=1 → 2키/일 × 3일 = 6키
        assertThat(compositeKeyCount).isEqualTo(6L);
    }

    // ── E3: TAX/SLIP + types + ETD만(perfDt 없음) → 전역 composite ─────────────

    @Test
    void TAX_types_ETD만_전역_composite_경로로_지원한다() {
        // perfDt 없음 + TAX + types → 전역 composite 버킷(ln:c:{t}{s}{i}:{TYPE})
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            null, null,
            List.of("INVOICE"),
            null, null, null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void SLIP_types_perfDt없음_전역_composite_경로로_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.SLIP_ISSUED,
            null, null,
            List.of("DEBIT"),
            null, null, null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void TAX_types_ETD만_전역_composite_키는_ln_c_패턴이다() {
        // TAX + INVOICE: t=1, s∈{0,1}, i∈{0,1} → 4키
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            null, null,
            List.of("INVOICE"),
            null, null, null, null
        );
        path.computeFreightCount(cmd, PREFIX);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(valueOps, atLeastOnce()).multiGet(captor.capture());

        List<String> allKeys = captor.getAllValues().stream()
            .flatMap(List::stream).toList();
        // 전역 composite: {p}:ln:c:{tsi}:{TYPE} (pd 없음)
        long globalCompositeCount = allKeys.stream()
            .filter(k -> k.contains(":ln:c:") && k.contains(":INVOICE"))
            .count();
        // TAX: t=1 고정, s∈{0,1}, i∈{0,1} → 4키
        assertThat(globalCompositeCount).isEqualTo(4L);
    }

    // ── E3: ETD + issued Y/N + perfDt 없음 → 전역 composite(i 고정) ──────────

    @Test
    void ETD_issued_Y_perfDt없음_전역_composite_i고정_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, null, null, "Y", null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void ETD_issued_N_perfDt없음_전역_composite_i고정_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, null, null, "N", null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void FREIGHT_issued_Y_perfDt없음_전역_composite_키는_i1_고정이다() {
        // FREIGHT+issued Y+타입미지정: t∈{0,1} × s∈{0,1} × i=1 × 5종(INVOICE/DEBIT/PAYMENT/CREDIT/none)
        // = 4변형 × 5종 = 20키
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, null, null, "Y", null
        );
        path.computeFreightCount(cmd, PREFIX);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(valueOps, atLeastOnce()).multiGet(captor.capture());

        List<String> allKeys = captor.getAllValues().stream()
            .flatMap(List::stream).toList();
        long globalKeys = allKeys.stream().filter(k -> k.contains(":ln:c:")).count();
        // i=1 고정 × t∈{0,1} × s∈{0,1} = 4변형, TYPE 5종(INVOICE/DEBIT/PAYMENT/CREDIT/none) = 20키
        assertThat(globalKeys).isEqualTo(20L);
        // i=0 키(…0:TYPE) 없음 확인 — issued Y → i=1 고정이므로 TSI 마지막 비트는 모두 1
        // 패턴 ":c:XX0:" 를 검색: 마지막 비트가 0인 키 없어야 함
        assertThat(allKeys.stream().filter(k -> k.matches(".*:ln:c:[01][01]0:.*")).count())
            .isEqualTo(0L);
    }

    // ── E3: FREIGHT + issued + 변형 수 가드 ──────────────────────────────────

    @Test
    void FREIGHT_issued_types_maxDayBuckets_곱하기8_초과하면_null_반환한다() {
        // maxDayBuckets=1로 설정, FREIGHT+issued+4타입 = 4키×4타입=16키 → 1×8=8 초과 → null
        props.getCountIndex().setMaxDayBuckets(1);
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            List.of("INVOICE", "PAYMENT", "DEBIT", "CREDIT"),
            null, null, "Y", null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNull();
    }

    @Test
    void FREIGHT_issued_타입없음_변형수가드_통과하면_비null_반환한다() {
        // maxDayBuckets=1500(기본), FREIGHT+issued+types 없음 = 4키(TYPE=none) → 가드 통과
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, null, null, "Y", null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    // ── 단일술어 단순버킷 fast-path 유지 검증 ────────────────────────────────

    @Test
    void FREIGHT_perfDt범위_types없음_단순버킷_has_freight_패턴_사용한다() {
        // 단일술어 없음(issued X, types X) + FREIGHT + perfDt → has-freight 단순버킷
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240103",
            null, null, null, null, null
        );
        path.computeFreightCount(cmd, PREFIX);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(valueOps, atLeastOnce()).multiGet(captor.capture());

        List<String> allKeys = captor.getAllValues().stream()
            .flatMap(List::stream).toList();
        // 단순 일버킷: {p}:ln:pd:{day}:has-freight 패턴 — composite 패턴(:c:) 없음
        assertThat(allKeys.stream().anyMatch(k -> k.contains(":ln:pd:") && k.endsWith(":has-freight")))
            .isTrue();
        assertThat(allKeys.stream().anyMatch(k -> k.contains(":ln:pd:") && k.contains(":c:")))
            .isFalse();
    }

    @Test
    void TAX_perfDt범위_types없음_단순버킷_경로이다() {
        // TAX basis는 hasLinePredicate=true(basis가 TAX이므로) → composite 경로
        // issued 없어도 TAX basis라서 composite 경로 진입
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240103",
            null, null, null, null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        // TAX+perfDt → composite 일버킷 경로(issued 없어도 basis 자체가 라인 술어)
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    // ── 기존 지원 경로 회귀 방지 ─────────────────────────────────────────────

    @Test
    void FREIGHT_perfDt_양쪽있으면_지원하고_비null을반환한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240105",
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void FREIGHT_기준에_documentTypes_있으면_지원한다() {
        // FREIGHT + documentTypes (issued 없음) → fdc 버킷 fast-path → non-null
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240131",
            List.of("INVOICE"),
            null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void TAX_basis_perfDt범위_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240131",
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void SLIP_basis_perfDt범위_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.SLIP_ISSUED,
            "20240101", "20240131",
            null, null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void perfDt없고_FREIGHT_documentTypes있으면_전역_composite_경로로_지원한다() {
        // issued 없고 FREIGHT+types → 전역 composite 경로(issued 없어 i 열거)
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            List.of("INVOICE"),
            null, null, null, null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void issued_Y_필터와_perfDt_있으면_composite_경로로_지원한다() {
        // W2: issued + perfDt → E3 composite 경로
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240105",
            null, null, null, "Y", null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    @Test
    void issued_N_필터와_perfDt_있으면_composite_경로로_지원한다() {
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240105",
            null, null, null, "N", null
        );
        assertThat(path.computeFreightCount(cmd, PREFIX)).isNotNull();
    }

    // ── E2: documentStatus + perfDt → doc AND 경로(승인된 조정) ──────────────

    @Test
    void documentStatus_있고_perfDt_범위_있으면_E2_doc_경로_진입후_0L을반환한다() {
        // E2 신규 동작: documentStatus 있어도 freight lineSet 계산 후 doc AND 시도.
        // mock 환경에서 loadDocAllFdIdBitmap이 빈 비트맵을 반환하지 않고 null을 반환하면
        // Mongo 폴백(null). 빈 비트맵 반환하도록 mock 추가 없이 기본 multiGet mock 활용.
        // dc:all 키도 빈 byte[] 반환 → deserialize → 빈 비트맵 → docOrdinals 빈 비트맵
        // → AND(lineSet, docOrdinals) cardinality = 0L.
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            "20240101", "20240103",
            null, null, "ISSUED", null, null
        );
        Long result = path.computeFreightCount(cmd, PREFIX);
        // E2 경로: doc AND 시도, dc:all 빈 비트맵 → cardinality 0L (non-null)
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0L);
    }

    // ── 결함1 회귀: 타입 미지정 시 5종(INVOICE/DEBIT/PAYMENT/CREDIT/none) 열거 ──

    @Test
    void TAX_타입미지정_perfDt_composite_키에_INVOICE_변형이_포함된다() {
        // 결함1 회귀: documentTypes 미지정(hasDocTypes=false)이면 FDC_ALL_TYPES 5종 열거.
        // 수정 전: none 버킷 1종만 생성 → INVOICE 라인이 집계에서 누락.
        // 수정 후: INVOICE 포함 5종 × TAX s∈{0,1} × i∈{0,1} = 20키.
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.TAX_ISSUED,
            "20240101", "20240101",
            null, null, null, null, null
        );
        path.computeFreightCount(cmd, PREFIX);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(valueOps, atLeastOnce()).multiGet(captor.capture());

        List<String> allKeys = captor.getAllValues().stream()
            .flatMap(List::stream).toList();
        // INVOICE 변형이 반드시 존재
        assertThat(allKeys.stream().anyMatch(k -> k.contains(":ln:pd:") && k.contains(":c:") && k.endsWith(":INVOICE")))
            .isTrue();
        // "none" 변형도 여전히 존재 (fdcType 없는 라인 포함)
        assertThat(allKeys.stream().anyMatch(k -> k.contains(":ln:pd:") && k.contains(":c:") && k.endsWith(":none")))
            .isTrue();
    }

    @Test
    void ETD_issued_타입미지정_전역_composite_키에_INVOICE_변형이_포함된다() {
        // 결함1 회귀: ETD+issued+타입미지정(전역 composite 경로) 동일 확인.
        SearchPmsPerformanceCommand cmd = buildCmd(
            AggregationBasis.FREIGHT_INPUT,
            null, null,
            null, null, null, "Y", null
        );
        path.computeFreightCount(cmd, PREFIX);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(valueOps, atLeastOnce()).multiGet(captor.capture());

        List<String> allKeys = captor.getAllValues().stream()
            .flatMap(List::stream).toList();
        // INVOICE 변형 존재 (전역 composite: {p}:ln:c:{tsi}:INVOICE)
        assertThat(allKeys.stream().anyMatch(k -> k.contains(":ln:c:") && k.endsWith(":INVOICE")))
            .isTrue();
        // "none" 변형도 존재
        assertThat(allKeys.stream().anyMatch(k -> k.contains(":ln:c:") && k.endsWith(":none")))
            .isTrue();
    }

    // ── 커맨드 생성 헬퍼 ──────────────────────────────────────────────────────

    /**
     * 18-field Command 생성자 헬퍼.
     * 테스트에서 자주 null로 두는 필드를 파라미터화하고 나머지는 고정값.
     */
    private SearchPmsPerformanceCommand buildCmd(
            AggregationBasis basis,
            String perfDtFrom,
            String perfDtTo,
            List<String> documentTypes,
            String documentDtFrom,
            String documentStatus,
            String issued,
            String grouped) {

        return new SearchPmsPerformanceCommand(
            basis, 0, 20,
            "SEA", "EXP",
            "ETD", null, null,
            perfDtFrom, perfDtTo,
            documentDtFrom, null,
            documentTypes, documentStatus,
            grouped, issued,
            null, null
        );
    }
}
