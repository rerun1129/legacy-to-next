package com.freightos.fms.application.masterbl;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.port.out.MasterBlPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MasterBlServiceTest {

    @Mock
    private MasterBlPort masterBlPort;

    @Mock
    private HouseBlPort houseBlPort;

    @InjectMocks
    private MasterBlService masterBlService;

    @Test
    @DisplayName("list - Bound.EXP 조회 시 port.getMasterBlsByBound(EXP) 위임")
    void list_exp_delegatesToPort() {
        PageRequest pageRequest = PageRequest.of(0, 50);
        PageRequest sortedRequest = PageRequest.of(0, 50, "createdAt", SortDirection.DESC);
        MasterBl mockEntity = mock(MasterBl.class);
        PagedResult<MasterBl> expected = PagedResult.of(List.of(mockEntity), 1L, 1, 0, 50);
        given(masterBlPort.getMasterBlsByBound(Bound.EXP, sortedRequest)).willReturn(expected);

        PagedResult<MasterBl> result = masterBlService.getMasterBlsByBound(Bound.EXP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(masterBlPort).should().getMasterBlsByBound(Bound.EXP, sortedRequest);
    }

    @Test
    @DisplayName("list - Bound.IMP 조회 시 port.getMasterBlsByBound(IMP) 위임")
    void list_imp_delegatesToPort() {
        PageRequest pageRequest = PageRequest.of(0, 50);
        PageRequest sortedRequest = PageRequest.of(0, 50, "createdAt", SortDirection.DESC);
        MasterBl mockEntity = mock(MasterBl.class);
        PagedResult<MasterBl> expected = PagedResult.of(List.of(mockEntity), 1L, 1, 0, 50);
        given(masterBlPort.getMasterBlsByBound(Bound.IMP, sortedRequest)).willReturn(expected);

        PagedResult<MasterBl> result = masterBlService.getMasterBlsByBound(Bound.IMP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(masterBlPort).should().getMasterBlsByBound(Bound.IMP, sortedRequest);
    }

    @Test
    @DisplayName("getById - 존재하는 ID 조회 시 엔티티 반환")
    void getById_existingId_returnsEntity() {
        Long id = 1L;
        MasterBl mockEntity = mock(MasterBl.class);
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(mockEntity));

        MasterBl result = masterBlService.findMasterBlById(id);

        assertThat(result).isEqualTo(mockEntity);
        then(masterBlPort).should().findMasterBlById(id);
    }

    @Test
    @DisplayName("getById - 존재하지 않는 ID 조회 시 ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.findMasterBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete - 존재하는 ID 삭제 시 port.delete 호출")
    void delete_existingId_callsPortDelete() {
        Long id = 1L;
        MasterBl mockEntity = mock(MasterBl.class);
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(mockEntity));

        masterBlService.deleteMasterBlById(id);

        then(masterBlPort).should().findMasterBlById(id);
        then(masterBlPort).should().deleteMasterBl(mockEntity);
    }

    // ── findMasterBlDetailById ────────────────────────────────

    @Test
    @DisplayName("findMasterBlDetailById(SEA): Sea 마스터 → Sea projection만 반환, Air port 미호출")
    void findMasterBlDetailById_seaMaster_returnsSeaSummariesAndCallsSeaPortOnly() {
        Long id = 1L;
        MasterBlSea master = MasterBlSea.create(Bound.EXP);
        ConsoledHouseBlSeaSummary s1 = new ConsoledHouseBlSeaSummary(1L, "HBL-001", "SHIP01", "CONS01", "DOC01", 10, "CTN", BigDecimal.valueOf(100), BigDecimal.valueOf(1), "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC");
        ConsoledHouseBlSeaSummary s2 = new ConsoledHouseBlSeaSummary(2L, "HBL-002", "SHIP02", "CONS02", "DOC02", 5, "PKG", BigDecimal.valueOf(50), BigDecimal.valueOf(0.5), "20251130", "20251201", "VESSEL B", "V002", "KRPUS", "JPOSA");
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledSeaSummariesByMasterBlId(id)).willReturn(List.of(s1, s2));

        MasterBlDetail result = masterBlService.findMasterBlDetailById(id);

        assertThat(result).isNotNull();
        assertThat(result.consolidatedHouseBls()).isNotNull().hasSize(2);
        assertThat(result.consolidatedHouseBls()).allMatch(s -> s instanceof ConsoledHouseBlSeaSummary);
        then(houseBlPort).should().findConsoledSeaSummariesByMasterBlId(id);
        then(houseBlPort).should(never()).findConsoledAirSummariesByMasterBlId(any());
    }

    @Test
    @DisplayName("findMasterBlDetailById(AIR): Air 마스터 → Air projection만 반환, Sea port 미호출")
    void findMasterBlDetailById_airMaster_returnsAirSummariesAndCallsAirPortOnly() {
        Long id = 2L;
        MasterBlAir master = MasterBlAir.create(Bound.EXP);
        ConsoledHouseBlAirSummary a1 = new ConsoledHouseBlAirSummary(10L, "HBL-A01", "SHIP01", "CONS01", "DOC01", 3, "PKG", BigDecimal.valueOf(30), BigDecimal.valueOf(0.3), BigDecimal.valueOf(45));
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledAirSummariesByMasterBlId(id)).willReturn(List.of(a1));

        MasterBlDetail result = masterBlService.findMasterBlDetailById(id);

        assertThat(result.consolidatedHouseBls()).isNotNull().hasSize(1);
        assertThat(result.consolidatedHouseBls()).allMatch(s -> s instanceof ConsoledHouseBlAirSummary);
        then(houseBlPort).should().findConsoledAirSummariesByMasterBlId(id);
        then(houseBlPort).should(never()).findConsoledSeaSummariesByMasterBlId(any());
    }

    @Test
    @DisplayName("findMasterBlDetailById(SEA, IMP): Bound와 무관하게 Sea 분기")
    void findMasterBlDetailById_seaImpMaster_routesToSeaProjection() {
        Long id = 3L;
        MasterBlSea master = MasterBlSea.create(Bound.IMP);
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledSeaSummariesByMasterBlId(id)).willReturn(List.of());

        MasterBlDetail result = masterBlService.findMasterBlDetailById(id);

        assertThat(result.consolidatedHouseBls()).isNotNull();
        then(houseBlPort).should().findConsoledSeaSummariesByMasterBlId(id);
        then(houseBlPort).should(never()).findConsoledAirSummariesByMasterBlId(any());
    }

    @Test
    @DisplayName("findMasterBlDetailById: 미지원 타입(Sea/Air 외) → 빈 리스트, port 미호출")
    void findMasterBlDetailById_unsupportedMasterType_returnsEmptyListAndCallsNoPort() {
        Long id = 4L;
        MasterBl unknownMaster = mock(MasterBl.class);
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(unknownMaster));

        MasterBlDetail result = masterBlService.findMasterBlDetailById(id);

        assertThat(result.consolidatedHouseBls()).isNotNull().isEmpty();
        then(houseBlPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("findMasterBlDetailById: 없는 id → ResourceNotFoundException, houseBlPort 절대 미호출")
    void findMasterBlDetailById_notFoundId_throwsResourceNotFoundExceptionAndSkipsHouseBlPort() {
        Long id = 999L;
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.findMasterBlDetailById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("findMasterBlDetailById(SEA): 연결 HouseBl 0건 → 빈 리스트(null 아님)")
    void findMasterBlDetailById_zeroHouseBls_returnsEmptyButNonNullList() {
        Long id = 5L;
        MasterBlSea master = MasterBlSea.create(Bound.EXP);
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledSeaSummariesByMasterBlId(id)).willReturn(List.of());

        MasterBlDetail result = masterBlService.findMasterBlDetailById(id);

        assertThat(result.consolidatedHouseBls()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("findMasterBlDetailById(SEA): port 반환 순서가 응답에 보존된다")
    void findMasterBlDetailById_seaProjections_preservesPortReturnOrder() {
        Long id = 6L;
        MasterBlSea master = MasterBlSea.create(Bound.EXP);
        ConsoledHouseBlSeaSummary first = new ConsoledHouseBlSeaSummary(1L, "HBL-FIRST", "SHIP01", "CONS01", "DOC01", 1, "CTN", BigDecimal.ONE, BigDecimal.ONE, "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC");
        ConsoledHouseBlSeaSummary second = new ConsoledHouseBlSeaSummary(2L, "HBL-SECOND", "SHIP02", "CONS02", "DOC02", 2, "CTN", BigDecimal.ONE, BigDecimal.ONE, "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC");
        ConsoledHouseBlSeaSummary third = new ConsoledHouseBlSeaSummary(3L, "HBL-THIRD", "SHIP03", "CONS03", "DOC03", 3, "CTN", BigDecimal.ONE, BigDecimal.ONE, "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC");
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledSeaSummariesByMasterBlId(id)).willReturn(List.of(first, second, third));

        MasterBlDetail result = masterBlService.findMasterBlDetailById(id);

        assertThat(result.consolidatedHouseBls()).containsExactly(first, second, third);
    }

    // ── 트랜잭션 메타 ────────────────────────────────────

    @Test
    @DisplayName("MasterBlService 클래스 레벨에 @Transactional(readOnly=true)가 선언되어 있다")
    void masterBlServiceClass_isAnnotatedWithTransactionalReadOnlyTrue() {
        Transactional annotation = MasterBlService.class.getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }

    @Test
    @DisplayName("deleteMasterBlById - 없는 ID 조회 시 ResourceNotFoundException throw, port.delete 미호출")
    void deleteMasterBlById_whenNotFound_throwsAndDoesNotDelete() {
        Long id = 999L;
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.deleteMasterBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(masterBlPort).should(never()).deleteMasterBl(any());
    }

    @Test
    @DisplayName("getMasterBlsByBound - port PagedResult 메타(totalElements/totalPages/page/size) 그대로 반영")
    void getMasterBlsByBound_returnsPagedResultWithCorrectMeta() {
        PageRequest pageRequest = PageRequest.of(1, 15);
        PageRequest sortedRequest = PageRequest.of(1, 15, "createdAt", SortDirection.DESC);
        MasterBl mockEntity = mock(MasterBl.class);
        PagedResult<MasterBl> portResult = PagedResult.of(List.of(mockEntity), 42L, 3, 1, 15);
        given(masterBlPort.getMasterBlsByBound(Bound.EXP, sortedRequest)).willReturn(portResult);

        PagedResult<MasterBl> result = masterBlService.getMasterBlsByBound(Bound.EXP, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(42L);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(15);
    }

    @Test
    @DisplayName("findMasterBlDetailById - 반환 MasterBlDetail.master()가 port에서 반환된 MasterBl과 동일")
    void findMasterBlDetailById_returnsMasterBlDetailWithCorrectMasterField() {
        Long id = 10L;
        MasterBlSea master = MasterBlSea.create(Bound.EXP);
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledSeaSummariesByMasterBlId(id)).willReturn(List.of());

        MasterBlDetail result = masterBlService.findMasterBlDetailById(id);

        assertThat(result.masterBl()).isEqualTo(master);
    }
}
