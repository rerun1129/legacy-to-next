package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PmsRedisExactCountProvider.collectBitmapKeys basis has-flag 키 포함 여부 단위 테스트.
 *
 * 버그 A 수정 검증:
 * - 차원/날짜 필터가 있을 때 basis별 has-flag 키가 AND 묶음에 포함되는지 확인한다.
 * - 차원/날짜가 모두 없을 때(무필터 전체 조회)는 여전히 빈 목록이 반환되는지 확인한다.
 *
 * 라이브 Redis/Mongo 없이 순수 로직만 검증한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 사용한다.
 */
class PmsRedisExactCountProviderBitmapKeyTest {

    private static final String PREFIX = "pms:ix";

    private PmsRedisExactCountProvider provider;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        PmsMartProperties props = new PmsMartProperties();
        props.getCountIndex().setKeyPrefix(PREFIX);
        props.getCountIndex().setMaxDistinctScan(2000000);

        RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);

        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        CircuitBreakerRegistry registry = mock(CircuitBreakerRegistry.class);
        when(registry.circuitBreaker(anyString())).thenReturn(circuitBreaker);

        provider = new PmsRedisExactCountProvider(redisTemplate, mongoTemplate, props, registry);
    }

    // ── basis has-flag 키 포함 검증 ───────────────────────────────────────────

    @Test
    void FREIGHT_INPUT_차원필터있으면_hasFlagFreight키가_AND묶음에포함된다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            "SEA", "EXP", "ETD", null, null,
            null, null, null, null,
            null, null, "CUST01", null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );

        List<String> keys = provider.collectBitmapKeys(cmd, PREFIX, cmd.dateKind(), cmd.dateFrom(), cmd.dateTo());

        assertThat(keys).isNotNull();
        String expectedFlagKey = PmsCountIndexKeys.hasFlagBitmap(PREFIX, PmsCountIndexKeys.FLAG_FREIGHT);
        assertThat(keys).contains(expectedFlagKey);
    }

    @Test
    void TAX_ISSUED_차원필터있으면_hasFlagTax키가_AND묶음에포함된다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.TAX_ISSUED, 0, 20,
            "SEA", "EXP", "ETD", null, null,
            null, null, null, null,
            null, null, "CUST01", null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );

        List<String> keys = provider.collectBitmapKeys(cmd, PREFIX, cmd.dateKind(), cmd.dateFrom(), cmd.dateTo());

        assertThat(keys).isNotNull();
        String expectedFlagKey = PmsCountIndexKeys.hasFlagBitmap(PREFIX, PmsCountIndexKeys.FLAG_TAX);
        assertThat(keys).contains(expectedFlagKey);
    }

    @Test
    void SLIP_ISSUED_차원필터있으면_hasFlagSlip키가_AND묶음에포함된다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.SLIP_ISSUED, 0, 20,
            "SEA", "EXP", "ETD", null, null,
            null, null, null, null,
            null, null, "CUST01", null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );

        List<String> keys = provider.collectBitmapKeys(cmd, PREFIX, cmd.dateKind(), cmd.dateFrom(), cmd.dateTo());

        assertThat(keys).isNotNull();
        String expectedFlagKey = PmsCountIndexKeys.hasFlagBitmap(PREFIX, PmsCountIndexKeys.FLAG_SLIP);
        assertThat(keys).contains(expectedFlagKey);
    }

    @Test
    void DOCUMENT_CREATED_차원필터있으면_hasFlagDoc키가_AND묶음에포함된다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.DOCUMENT_CREATED, 0, 20,
            "SEA", "EXP", "ETD", null, null,
            null, null, null, null,
            null, null, "CUST01", null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );

        List<String> keys = provider.collectBitmapKeys(cmd, PREFIX, cmd.dateKind(), cmd.dateFrom(), cmd.dateTo());

        assertThat(keys).isNotNull();
        String expectedFlagKey = PmsCountIndexKeys.hasFlagBitmap(PREFIX, PmsCountIndexKeys.FLAG_DOC);
        assertThat(keys).contains(expectedFlagKey);
    }

    @Test
    void 날짜필터만있으면_hasFlagFreight키가_AND묶음에포함된다() {
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, "ETD", "20240101", "20240131",
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );

        List<String> keys = provider.collectBitmapKeys(cmd, PREFIX, cmd.dateKind(), cmd.dateFrom(), cmd.dateTo());

        assertThat(keys).isNotNull();
        String expectedFlagKey = PmsCountIndexKeys.hasFlagBitmap(PREFIX, PmsCountIndexKeys.FLAG_FREIGHT);
        assertThat(keys).contains(expectedFlagKey);
    }

    @Test
    void 차원과날짜필터가모두없으면_빈목록을반환한다() {
        // 무필터 전체 조회: 기존 동작 유지 — Mongo가 전체 count에 더 빠름
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );

        List<String> keys = provider.collectBitmapKeys(cmd, PREFIX, cmd.dateKind(), cmd.dateFrom(), cmd.dateTo());

        // 빈 목록: 상위 isEmpty 가드에서 null 반환(Mongo 폴백)으로 이어진다
        assertThat(keys).isNotNull().isEmpty();
    }

    @Test
    void hasFlagKey는_dimKeys에분류되어_AND교집합에포함된다() {
        // has-flag 키는 ":bl:has:" 패턴 — etd/eta 키와 다르므로 dateKeys가 아닌 dimKeys로 분류됨
        // 이 테스트는 has-flag 키가 날짜 OR 집합이 아닌 AND 집합에 포함되는지 구조 확인 목적
        SearchPmsPerformanceCommand cmd = new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, "ETD", "20240101", "20240103",
            null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null
        );

        List<String> keys = provider.collectBitmapKeys(cmd, PREFIX, cmd.dateKind(), cmd.dateFrom(), cmd.dateTo());

        assertThat(keys).isNotNull();
        String flagKey = PmsCountIndexKeys.hasFlagBitmap(PREFIX, PmsCountIndexKeys.FLAG_FREIGHT);
        assertThat(keys).contains(flagKey);
        // flagKey는 :bl:etd:·:bl:eta: 패턴이 아니므로 dimKeys(AND)에 속해야 한다
        assertThat(flagKey).doesNotContain(":bl:etd:").doesNotContain(":bl:eta:");
    }
}
