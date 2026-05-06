package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.masterbl.projection.ConsoledHouseBlSummaryView;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterBlDetailResponseTest {

    // ── record 구조 검증 ─────────────────────────────────────────────

    @Test
    @DisplayName("MasterBlDetailResponse: record 컴포넌트가 정확히 23개이고, 23번째는 consolidatedHouseBls이다")
    void recordComponents_haveExactlyTwentyThreeFields() {
        var components = MasterBlDetailResponse.class.getRecordComponents();

        assertThat(components).hasSize(23);
        assertThat(components[22].getName()).isEqualTo("consolidatedHouseBls");
        assertThat(components[22].getType()).isEqualTo(List.class);
    }

    // ── from(MasterBlDetailResult) 매핑 검증 ─────────────────────────

    @Test
    @DisplayName("from(MasterBlDetailResult): 콘솔 리스트가 응답 마지막 필드까지 매핑된다")
    void from_withMasterBlDetailResultContainingSea_mapsAllFieldsAndConsolidatedList() {
        ConsoledHouseBlSummaryView seaSummary = new ConsoledHouseBlSummaryView(
                1L, "HBL-001", "SHIP01", "CONS01", "DOC01",
                10, "CTN", BigDecimal.valueOf(100), BigDecimal.valueOf(1),
                "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC",
                null
        );
        MasterBlDetailResult result = new MasterBlDetailResult(
                1L, "MBL-001", "REF-001",
                "SEA", "EXP", "HOUSE",
                "SHIP01", "CONS01", "NOTIFY01",
                "KRPUS", "USNYC", "20251130", "20251201",
                "PREPAID", "OPR01", "TEAM01",
                10, "KGS", BigDecimal.valueOf(100), BigDecimal.valueOf(1),
                null, null,
                List.of(seaSummary)
        );

        MasterBlDetailResponse response = MasterBlDetailResponse.from(result);

        assertThat(response).isNotNull();
        assertThat(response.consolidatedHouseBls()).hasSize(1);
        assertThat(response.consolidatedHouseBls().get(0)).isEqualTo(seaSummary);
    }

    @Test
    @DisplayName("from(MasterBlDetailResult): 빈 consolidated 리스트도 그대로 보존(null 변환 안 함)")
    void from_withEmptyConsolidatedList_returnsResponseWithEmptyList() {
        MasterBlDetailResult result = new MasterBlDetailResult(
                2L, "MBL-002", null,
                "SEA", "EXP", "HOUSE",
                null, null, null,
                null, null, null, null,
                "PREPAID", null, null,
                null, null, null, null,
                null, null,
                List.of()
        );

        MasterBlDetailResponse response = MasterBlDetailResponse.from(result);

        assertThat(response.consolidatedHouseBls()).isNotNull().isEmpty();
    }
}
