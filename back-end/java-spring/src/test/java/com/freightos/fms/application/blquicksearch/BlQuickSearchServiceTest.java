package com.freightos.fms.application.blquicksearch;

import com.freightos.fms.application.blquicksearch.command.BlQuickSearchCommand;
import com.freightos.fms.application.blquicksearch.port.out.BlQuickSearchPort;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import com.freightos.fms.domain.blquicksearch.BlQuickSearchFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BlQuickSearchServiceTest {

    @Mock
    private BlQuickSearchPort blQuickSearchPort;

    @InjectMocks
    private BlQuickSearchService blQuickSearchService;

    private static BlQuickSearchSummary summary(Long id, String blType, String blNo) {
        return new BlQuickSearchSummary(id, blType, blNo, "SEA", "EXP", "SHIP01", "KRPUS", "USLAX", "20260101");
    }

    private static BlQuickSearchCommand cmdWith(String jobDiv, String salesManCode) {
        return new BlQuickSearchCommand(null, jobDiv, null, null, null, null, null, null, salesManCode, null, null, null, null, null);
    }

    // ── Master 실행 제외 분기 ────────────────────────────────────────

    @Test
    @DisplayName("jobDiv=TRUCK → Master searchMaster 호출 안 함")
    void quickSearch_truckJobDiv_skipsMaster() {
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(List.of());

        blQuickSearchService.quickSearch(cmdWith("TRUCK", null));

        then(blQuickSearchPort).should(never()).searchMaster(any(), anyInt());
    }

    @Test
    @DisplayName("jobDiv=NON_BL → Master searchMaster 호출 안 함")
    void quickSearch_nonBlJobDiv_skipsMaster() {
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(List.of());

        blQuickSearchService.quickSearch(cmdWith("NON_BL", null));

        then(blQuickSearchPort).should(never()).searchMaster(any(), anyInt());
    }

    @Test
    @DisplayName("salesManCode 설정 시 → Master searchMaster 호출 안 함")
    void quickSearch_salesManCodeSet_skipsMaster() {
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(List.of());

        blQuickSearchService.quickSearch(cmdWith(null, "SM001"));

        then(blQuickSearchPort).should(never()).searchMaster(any(), anyInt());
    }

    @Test
    @DisplayName("jobDiv=SEA, salesManCode 공란 → House/Master 모두 호출")
    void quickSearch_seaJobDiv_callsBoth() {
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(List.of());
        given(blQuickSearchPort.searchMaster(any(), anyInt())).willReturn(List.of());

        blQuickSearchService.quickSearch(cmdWith("SEA", null));

        then(blQuickSearchPort).should().searchHouse(any(), anyInt());
        then(blQuickSearchPort).should().searchMaster(any(), anyInt());
    }

    @Test
    @DisplayName("jobDiv=AIR, salesManCode 공란 → House/Master 모두 호출")
    void quickSearch_airJobDiv_callsBoth() {
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(List.of());
        given(blQuickSearchPort.searchMaster(any(), anyInt())).willReturn(List.of());

        blQuickSearchService.quickSearch(cmdWith("AIR", null));

        then(blQuickSearchPort).should().searchHouse(any(), anyInt());
        then(blQuickSearchPort).should().searchMaster(any(), anyInt());
    }

    // ── 병합·정렬 검증 ──────────────────────────────────────────────

    @Test
    @DisplayName("병합 후 blNo ASC → blType ASC → id ASC 정렬")
    void quickSearch_mergedAndSortedByBlNoThenBlTypeThenId() {
        BlQuickSearchSummary house1 = summary(10L, "HOUSE", "BL-BBB");
        BlQuickSearchSummary house2 = summary(20L, "HOUSE", "BL-AAA");
        BlQuickSearchSummary master1 = new BlQuickSearchSummary(5L, "MASTER", "BL-AAA", "SEA", "EXP", "SHIP01", "KRPUS", "USLAX", "20260101");
        BlQuickSearchSummary master2 = new BlQuickSearchSummary(3L, "MASTER", "BL-AAA", "SEA", "EXP", "SHIP01", "KRPUS", "USLAX", "20260101");

        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(List.of(house1, house2));
        given(blQuickSearchPort.searchMaster(any(), anyInt())).willReturn(List.of(master1, master2));

        BlQuickSearchCommand cmd = new BlQuickSearchCommand(null, "SEA", null, null, null, null, null, null, null, null, null, null, null, null);
        List<BlQuickSearchSummary> result = blQuickSearchService.quickSearch(cmd);

        // BL-AAA 먼저, HOUSE < MASTER 문자열 자연순, id ASC
        assertThat(result).hasSize(4);
        assertThat(result.get(0).blNo()).isEqualTo("BL-AAA");
        assertThat(result.get(0).blType()).isEqualTo("HOUSE");
        assertThat(result.get(0).id()).isEqualTo(20L);
        assertThat(result.get(1).blNo()).isEqualTo("BL-AAA");
        assertThat(result.get(1).blType()).isEqualTo("MASTER");
        assertThat(result.get(1).id()).isEqualTo(3L);
        assertThat(result.get(2).blNo()).isEqualTo("BL-AAA");
        assertThat(result.get(2).blType()).isEqualTo("MASTER");
        assertThat(result.get(2).id()).isEqualTo(5L);
        assertThat(result.get(3).blNo()).isEqualTo("BL-BBB");
    }

    // ── limit 상한 검증 ──────────────────────────────────────────────

    @Test
    @DisplayName("limit=null → 20 적용")
    void quickSearch_nullLimit_defaults20() {
        List<BlQuickSearchSummary> many = java.util.stream.IntStream.rangeClosed(1, 25)
                .mapToObj(i -> summary((long) i, "HOUSE", "BL-" + String.format("%03d", i)))
                .toList();
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(many);
        given(blQuickSearchPort.searchMaster(any(), anyInt())).willReturn(List.of());

        BlQuickSearchCommand cmd = new BlQuickSearchCommand(null, "SEA", null, null, null, null, null, null, null, null, null, null, null, null);
        List<BlQuickSearchSummary> result = blQuickSearchService.quickSearch(cmd);

        assertThat(result).hasSize(20);
    }

    @Test
    @DisplayName("limit=50 (MAX 초과) → 20으로 클램핑")
    void quickSearch_limitOver20_clampsTo20() {
        List<BlQuickSearchSummary> many = java.util.stream.IntStream.rangeClosed(1, 25)
                .mapToObj(i -> summary((long) i, "HOUSE", "BL-" + String.format("%03d", i)))
                .toList();
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(many);
        given(blQuickSearchPort.searchMaster(any(), anyInt())).willReturn(List.of());

        BlQuickSearchCommand cmd = new BlQuickSearchCommand(null, "SEA", null, null, null, null, null, null, null, null, null, null, null, 50);
        List<BlQuickSearchSummary> result = blQuickSearchService.quickSearch(cmd);

        assertThat(result).hasSize(20);
    }

    @Test
    @DisplayName("limit=5 → 5건만 반환")
    void quickSearch_limit5_returns5() {
        List<BlQuickSearchSummary> many = java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(i -> summary((long) i, "HOUSE", "BL-" + String.format("%03d", i)))
                .toList();
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(many);
        given(blQuickSearchPort.searchMaster(any(), anyInt())).willReturn(List.of());

        BlQuickSearchCommand cmd = new BlQuickSearchCommand(null, "SEA", null, null, null, null, null, null, null, null, null, null, null, 5);
        List<BlQuickSearchSummary> result = blQuickSearchService.quickSearch(cmd);

        assertThat(result).hasSize(5);
    }

    // ── Port에 전달되는 filter 검증 ──────────────────────────────────

    @Test
    @DisplayName("커맨드 필드가 filter로 올바르게 전달된다")
    void quickSearch_commandFieldsAreMappedToFilter() {
        given(blQuickSearchPort.searchHouse(any(), anyInt())).willReturn(List.of());
        given(blQuickSearchPort.searchMaster(any(), anyInt())).willReturn(List.of());

        BlQuickSearchCommand cmd = new BlQuickSearchCommand(
                "BLNO-QUERY", "SEA", "EXP", "ETD", "20260101", "20260131",
                "TM01", "OP01", null, "KRPUS", "USLAX", "SHIPPER", "SHIP01", null);
        blQuickSearchService.quickSearch(cmd);

        ArgumentCaptor<BlQuickSearchFilter> captor = ArgumentCaptor.forClass(BlQuickSearchFilter.class);
        then(blQuickSearchPort).should().searchHouse(captor.capture(), anyInt());
        BlQuickSearchFilter captured = captor.getValue();
        assertThat(captured.blNo()).isEqualTo("BLNO-QUERY");
        assertThat(captured.polCode()).isEqualTo("KRPUS");
        assertThat(captured.podCode()).isEqualTo("USLAX");
        assertThat(captured.teamCode()).isEqualTo("TM01");
    }
}
