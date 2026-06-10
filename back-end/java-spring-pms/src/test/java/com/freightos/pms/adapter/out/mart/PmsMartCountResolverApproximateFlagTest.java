package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.countindex.PmsRedisExactCountProvider;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PmsMartCountResolver의 approximate 플래그 단위 테스트.
 *
 * approx 분기: approximate=true
 * - 캐시 miss + Redis miss + approxEstimator >= threshold 시
 *
 * 정확 분기: approximate=false
 * - 캐시 exact 히트
 * - Redis count-index 성공
 * - approxEstimator < threshold (희소 폴백)
 * - approxEstimator 없음(line-accel OFF)
 * - exactCount=true 요청
 *
 * 라이브 Mongo/Redis 없이 Mockito mock만 사용한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 테스트한다.
 */
class PmsMartCountResolverApproximateFlagTest {

    private MongoTemplate mongoTemplate;
    private PmsMartProperties props;
    private PmsMartApproxCountEstimator approxEstimator;
    private PmsPerformanceQueryCache queryCache;
    private PmsRedisExactCountProvider redisCount;

    @BeforeEach
    void setUp() {
        mongoTemplate   = mock(MongoTemplate.class);
        approxEstimator = mock(PmsMartApproxCountEstimator.class);
        queryCache      = mock(PmsPerformanceQueryCache.class);
        redisCount      = mock(PmsRedisExactCountProvider.class);

        props = new PmsMartProperties();
        // earlyTermThreshold 기본값(PmsMartProperties 기본 생성자 값) 사용.
        // 근사 분기 검증을 위해 approx를 threshold 이상 반환한다.
    }

    // ── ★ approximate=true 되는 유일한 분기 ────────────────────────────────────

    @Test
    void resolveFreightTotal_approxEstimator가threshold이상이면_approximate가true이다() {
        // Redis miss, 캐시 miss
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal(any())).thenReturn(null);

        long threshold = props.getLineAccel().getEarlyTermThreshold();
        when(approxEstimator.estimate(any(Criteria.class))).thenReturn(threshold + 1);

        PmsMartCountResolver resolver = buildResolver(Optional.of(approxEstimator));

        ResolvedTotal result = resolver.resolveFreightTotal(
            nonDocLineFastCmd(), "hasFreightInput", new Criteria(), "key", "user", "sig");

        assertThat(result.approximate()).isTrue();
        assertThat(result.total()).isEqualTo(threshold + 1);
    }

    @Test
    void resolveFastPathTotal_approxEstimator가threshold이상이면_approximate가true이다() {
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal(any())).thenReturn(null);

        long threshold = props.getLineAccel().getEarlyTermThreshold();
        when(approxEstimator.estimate(any(Criteria.class))).thenReturn(threshold + 1);

        PmsMartCountResolver resolver = buildResolver(Optional.of(approxEstimator));

        ResolvedTotal result = resolver.resolveFastPathTotal(new Criteria(), noExactCmd(), "key");

        assertThat(result.approximate()).isTrue();
    }

    @Test
    void resolveDocumentTotal_approxEstimator가threshold이상이면_approximate가true이다() {
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal(any())).thenReturn(null);

        long threshold = props.getLineAccel().getEarlyTermThreshold();
        when(approxEstimator.estimate(any(Criteria.class))).thenReturn(threshold + 1);

        PmsMartCountResolver resolver = buildResolver(Optional.of(approxEstimator));

        ResolvedTotal result = resolver.resolveDocumentTotal(
            nonDocLineFastCmd(), new Criteria(), "key", "user", "sig");

        assertThat(result.approximate()).isTrue();
    }

    // ── 캐시 approx 히트 → approximate=true ─────────────────────────────────

    @Test
    void resolveFastPathTotal_캐시가approx히트이면_approximate가true이다() {
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal("key")).thenReturn(ResolvedTotal.approx(9999L));

        PmsMartCountResolver resolver = buildResolver(Optional.of(approxEstimator));

        ResolvedTotal result = resolver.resolveFastPathTotal(new Criteria(), noExactCmd(), "key");

        assertThat(result.approximate()).isTrue();
        assertThat(result.total()).isEqualTo(9999L);
    }

    // ── approximate=false 가 되는 분기들 ─────────────────────────────────────

    @Test
    void resolveFastPathTotal_lineAccelOff이면_approximate가false이다() {
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal(any())).thenReturn(null);
        // approxEstimator 없음 → line-accel OFF 분기
        when(mongoTemplate.count(any(), any(Class.class))).thenReturn(500L);

        // approxEstimator=empty 이면 line-accel OFF 경로
        PmsMartCountResolver resolver = buildResolver(Optional.empty());

        ResolvedTotal result = resolver.resolveFastPathTotal(new Criteria(), noExactCmd(), "key");

        assertThat(result.approximate()).isFalse();
        assertThat(result.total()).isEqualTo(500L);
    }

    @Test
    void resolveFastPathTotal_exactCountTrue이면_approximate가false이다() {
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal(any())).thenReturn(null);
        when(mongoTemplate.count(any(), any(Class.class))).thenReturn(300L);

        PmsMartCountResolver resolver = buildResolver(Optional.of(approxEstimator));

        ResolvedTotal result = resolver.resolveFastPathTotal(new Criteria(), exactCountCmd(), "key");

        assertThat(result.approximate()).isFalse();
        assertThat(result.total()).isEqualTo(300L);
    }

    @Test
    void resolveFastPathTotal_희소폴백이면_approximate가false이다() {
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal(any())).thenReturn(null);

        long threshold = props.getLineAccel().getEarlyTermThreshold();
        // threshold 미만 → 희소 폴백(exact)
        when(approxEstimator.estimate(any(Criteria.class))).thenReturn(threshold - 1);
        when(mongoTemplate.count(any(), any(Class.class))).thenReturn(threshold - 1);

        PmsMartCountResolver resolver = buildResolver(Optional.of(approxEstimator));

        ResolvedTotal result = resolver.resolveFastPathTotal(new Criteria(), noExactCmd(), "key");

        assertThat(result.approximate()).isFalse();
    }

    @Test
    void resolveFastPathTotal_캐시exact히트이면_approximate가false이다() {
        when(redisCount.isReady()).thenReturn(false);
        when(queryCache.getResolvedTotal("key")).thenReturn(ResolvedTotal.exact(1234L));

        PmsMartCountResolver resolver = buildResolver(Optional.of(approxEstimator));

        ResolvedTotal result = resolver.resolveFastPathTotal(new Criteria(), noExactCmd(), "key");

        assertThat(result.approximate()).isFalse();
        assertThat(result.total()).isEqualTo(1234L);
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    /** exactCount=null(기본) + 날짜·차원 필터 없음 커맨드. */
    private SearchPmsPerformanceCommand noExactCmd() {
        return new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, null, "20240101", "20240131",
            null, null, null, null,
            null, null, null, null
        );
    }

    /** exactCount=true 커맨드. */
    private SearchPmsPerformanceCommand exactCountCmd() {
        return new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, null, "20240101", "20240131",
            null, null, null, null,
            null, null, true, null
        );
    }

    /** 서류조건 없는 기본 커맨드(nonDocLine). */
    private SearchPmsPerformanceCommand nonDocLineFastCmd() {
        return noExactCmd();
    }

    private PmsMartCountResolver buildResolver(Optional<PmsMartApproxCountEstimator> estimator) {
        return new PmsMartCountResolver(
            mongoTemplate,
            props,
            Optional.empty(),   // planner — 테스트 대상 분기에서 불필요
            estimator,
            Optional.of(queryCache),
            Optional.empty(),   // exactCountRegistry
            Optional.of(redisCount)
        );
    }
}
