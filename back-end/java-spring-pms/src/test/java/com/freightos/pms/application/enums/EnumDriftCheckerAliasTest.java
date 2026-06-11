package com.freightos.pms.application.enums;

import com.freightos.pms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.pms.application.enums.projection.EnumOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * EnumDriftChecker 별칭 적용 단위 테스트.
 * @SpringBootTest 금지 — 순수 JUnit5 + Mockito.
 * 비결정적 요소 없음.
 */
@ExtendWith(MockitoExtension.class)
class EnumDriftCheckerAliasTest {

    @Mock
    private CommonCodeReadPort dbPort;

    private static final String API_KEY  = "JobDiv";
    private static final String DB_GROUP = "housebl.JobDiv";

    private EnumDriftChecker buildChecker(Map<String, List<EnumOption>> registryStore) {
        EnumRegistry registry = EnumRegistry.of(registryStore);
        return new EnumDriftChecker(registry, dbPort);
    }

    @Test
    @DisplayName("JobDiv 드리프트 체크 시 DB 조회는 housebl.JobDiv 그룹으로 수행된다")
    void jobDiv_driftCheck_usesDbGroup() {
        List<EnumOption> javaOptions = List.of(
                new EnumOption("IMPORT", "Import", null, "수입"),
                new EnumOption("EXPORT", "Export", null, "수출")
        );
        EnumDriftChecker checker = buildChecker(Map.of(API_KEY, javaOptions));
        given(dbPort.findByGroupCode(DB_GROUP)).willReturn(Optional.of(javaOptions));

        checker.checkDrift();

        then(dbPort).should().findByGroupCode(DB_GROUP);
        then(dbPort).should(never()).findByGroupCode(API_KEY);
    }

    @Test
    @DisplayName("JobDiv DB 미스(그룹 없음) — 원 API 키로는 호출 안 하고 드리프트 체크 조용히 skip")
    void jobDiv_dbMiss_skipsDriftCheck() {
        List<EnumOption> javaOptions = List.of(
                new EnumOption("IMPORT", "Import", null, "수입")
        );
        EnumDriftChecker checker = buildChecker(Map.of(API_KEY, javaOptions));
        // DB에 housebl.JobDiv 없음 → Optional.empty
        given(dbPort.findByGroupCode(DB_GROUP)).willReturn(Optional.empty());

        checker.checkDrift();

        then(dbPort).should().findByGroupCode(DB_GROUP);
        then(dbPort).should(never()).findByGroupCode(API_KEY);
    }

    @Test
    @DisplayName("비별칭 키(Bound) 드리프트 체크 시 DB 조회는 원 키 그대로 수행된다")
    void nonAliasKey_driftCheck_usesOriginalKey() {
        String nonAliasKey = "Bound";
        List<EnumOption> javaOptions = List.of(
                new EnumOption("INBOUND", "Inbound", null, "인바운드")
        );
        EnumDriftChecker checker = buildChecker(Map.of(nonAliasKey, javaOptions));
        given(dbPort.findByGroupCode(nonAliasKey)).willReturn(Optional.of(javaOptions));

        checker.checkDrift();

        then(dbPort).should().findByGroupCode(nonAliasKey);
    }
}
