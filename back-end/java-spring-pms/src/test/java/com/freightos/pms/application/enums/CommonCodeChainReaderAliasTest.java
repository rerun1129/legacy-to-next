package com.freightos.pms.application.enums;

import com.freightos.pms.application.enums.port.out.CommonCodeCachePort;
import com.freightos.pms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.pms.application.enums.projection.EnumOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * CommonCodeChainReader 별칭 적용 단위 테스트.
 * @SpringBootTest 금지 — 순수 JUnit5 + Mockito.
 * 비결정적 요소 없음.
 */
@ExtendWith(MockitoExtension.class)
class CommonCodeChainReaderAliasTest {

    @Mock
    private CommonCodeCachePort cachePort;
    @Mock
    private CommonCodeReadPort dbPort;

    private CommonCodeChainReader chainReader;

    private static final String API_KEY     = "JobDiv";
    private static final String DB_GROUP    = "housebl.JobDiv";

    private static final List<EnumOption> SAMPLE_OPTIONS = List.of(
            new EnumOption("IMPORT", "Import", null, "수입"),
            new EnumOption("EXPORT", "Export", null, "수출")
    );

    @BeforeEach
    void setUp() {
        chainReader = new CommonCodeChainReader(cachePort, dbPort);
    }

    // ── 별칭 키(JobDiv) 경로 ────────────────────────────────────────────────────

    @Test
    @DisplayName("별칭 키 캐시 히트 — cachePort.get은 housebl.JobDiv로 호출, DB는 미호출")
    void aliasKey_cacheHit_usesDbGroup() {
        given(cachePort.get(DB_GROUP)).willReturn(Optional.of(SAMPLE_OPTIONS));

        Optional<List<EnumOption>> result = chainReader.resolve(API_KEY);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(SAMPLE_OPTIONS);
        then(cachePort).should().get(DB_GROUP);
        then(dbPort).should(never()).findByGroupCode(DB_GROUP);
    }

    @Test
    @DisplayName("별칭 키 캐시 미스 + DB 히트 — dbPort.findByGroupCode는 housebl.JobDiv로 호출, read-through put도 housebl.JobDiv")
    void aliasKey_cacheMiss_dbHit_usesDbGroupForBoth() {
        given(cachePort.get(DB_GROUP)).willReturn(Optional.empty());
        given(dbPort.findByGroupCode(DB_GROUP)).willReturn(Optional.of(SAMPLE_OPTIONS));

        Optional<List<EnumOption>> result = chainReader.resolve(API_KEY);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(SAMPLE_OPTIONS);
        then(cachePort).should().get(DB_GROUP);
        then(dbPort).should().findByGroupCode(DB_GROUP);
        then(cachePort).should().put(DB_GROUP, SAMPLE_OPTIONS);
    }

    @Test
    @DisplayName("별칭 키 캐시 미스 + DB 미스 — Optional.empty 반환, put은 미호출")
    void aliasKey_cacheMiss_dbMiss_returnsEmpty() {
        given(cachePort.get(DB_GROUP)).willReturn(Optional.empty());
        given(dbPort.findByGroupCode(DB_GROUP)).willReturn(Optional.empty());

        Optional<List<EnumOption>> result = chainReader.resolve(API_KEY);

        assertThat(result).isEmpty();
        then(cachePort).should(never()).put(DB_GROUP, List.of());
    }

    @Test
    @DisplayName("별칭 키 조회 시 원 API 키로 cache/db를 호출하지 않는다")
    void aliasKey_neverUsesApiKey() {
        given(cachePort.get(DB_GROUP)).willReturn(Optional.of(SAMPLE_OPTIONS));

        chainReader.resolve(API_KEY);

        then(cachePort).should(never()).get(API_KEY);
        then(dbPort).should(never()).findByGroupCode(API_KEY);
    }

    // ── 비별칭 키(AggregationBasis) 경로 ────────────────────────────────────────

    @Test
    @DisplayName("비별칭 키 캐시 히트 — 원 키 그대로 cachePort.get 호출")
    void nonAliasKey_cacheHit_usesOriginalKey() {
        String nonAliasKey = "AggregationBasis";
        given(cachePort.get(nonAliasKey)).willReturn(Optional.of(SAMPLE_OPTIONS));

        Optional<List<EnumOption>> result = chainReader.resolve(nonAliasKey);

        assertThat(result).isPresent();
        then(cachePort).should().get(nonAliasKey);
        then(dbPort).should(never()).findByGroupCode(nonAliasKey);
    }

    @Test
    @DisplayName("비별칭 키 캐시 미스 + DB 히트 — 원 키로 DB 조회 및 read-through put")
    void nonAliasKey_cacheMiss_dbHit_usesOriginalKey() {
        String nonAliasKey = "Bound";
        given(cachePort.get(nonAliasKey)).willReturn(Optional.empty());
        given(dbPort.findByGroupCode(nonAliasKey)).willReturn(Optional.of(SAMPLE_OPTIONS));

        Optional<List<EnumOption>> result = chainReader.resolve(nonAliasKey);

        assertThat(result).isPresent();
        then(dbPort).should().findByGroupCode(nonAliasKey);
        then(cachePort).should().put(nonAliasKey, SAMPLE_OPTIONS);
    }
}
