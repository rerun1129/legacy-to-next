package com.freightos.fms.application.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailView;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.application.masterbl.port.out.MasterBlPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

    @Mock
    private MasterBlFactory masterBlFactory;

    @Mock
    private CodeNameResolver codeNameResolver;

    @InjectMocks
    private MasterBlService masterBlService;

    @Test
    @DisplayName("findMasterBlById - 존재하는 ID 조회 시 MasterBlDetailView(base+hsCodeName) 반환")
    void findMasterBlById_existingId_returnsDetailView() {
        Long id = 1L;
        MasterBlSea mockEntity = MasterBlSea.create(Bound.EXP);
        MasterBlDetailResult mockBase = mock(MasterBlDetailResult.class);
        given(mockBase.hsCode()).willReturn("8471.30");
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(mockEntity));
        given(houseBlPort.findConsoledSeaSummariesByMasterBlId(id)).willReturn(List.of());
        given(houseBlPort.findConsoledSeaContainersByMasterBlId(id)).willReturn(List.of());
        given(masterBlFactory.toDetailResult(mockEntity, List.of(), List.of())).willReturn(mockBase);
        given(codeNameResolver.findHsCodeNames(any())).willReturn(Map.of("8471.30", "컴퓨터"));

        MasterBlDetailView view = masterBlService.findMasterBlById(id);

        assertThat(view.base()).isEqualTo(mockBase);
        assertThat(view.hsCodeName()).isEqualTo("컴퓨터");
        then(masterBlPort).should().findMasterBlById(id);
        then(codeNameResolver).should().findHsCodeNames(any());
    }

    @Test
    @DisplayName("findMasterBlById - 존재하지 않는 ID 조회 시 ResourceNotFoundException")
    void findMasterBlById_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.findMasterBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteMasterBlById - jobDiv projection 후 port.deleteByIdAndJobDiv 호출")
    void delete_existingId_callsPortDelete() {
        Long id = 1L;
        given(masterBlPort.findJobDivById(id)).willReturn(Optional.of(MasterBlJobDiv.SEA));

        masterBlService.deleteMasterBlById(id);

        then(masterBlPort).should().findJobDivById(id);
        then(masterBlPort).should().deleteByIdAndJobDiv(id, MasterBlJobDiv.SEA);
        then(masterBlPort).should(never()).findMasterBlById(any());
    }

    // ── findMasterBlById (Sea/Air 분기) ───────────────────────────────

    @Test
    @DisplayName("findMasterBlById(SEA): Sea 마스터 → Sea projection 로드, Air port 미호출")
    void findMasterBlById_seaMaster_loadsSeaSummaries() {
        Long id = 1L;
        MasterBlSea master = MasterBlSea.create(Bound.EXP);
        ConsoledHouseBlSeaSummary s1 = new ConsoledHouseBlSeaSummary(1L, "HBL-001", "SHIP01", "CONS01", "DOC01", 10, "CTN", null, BigDecimal.valueOf(100), BigDecimal.valueOf(1), "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC");
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledSeaSummariesByMasterBlId(id)).willReturn(List.of(s1));
        given(masterBlFactory.toDetailResult(any(), any(), any())).willReturn(mock(MasterBlDetailResult.class));

        masterBlService.findMasterBlById(id);

        then(houseBlPort).should().findConsoledSeaSummariesByMasterBlId(id);
        then(houseBlPort).should(never()).findConsoledAirSummariesByMasterBlId(any());
    }

    @Test
    @DisplayName("findMasterBlById(AIR): Air 마스터 → Air projection 로드, Sea port 미호출")
    void findMasterBlById_airMaster_loadsAirSummaries() {
        Long id = 2L;
        MasterBlAir master = MasterBlAir.create(Bound.EXP);
        ConsoledHouseBlAirSummary a1 = new ConsoledHouseBlAirSummary(10L, "HBL-A01", "SHIP01", "CONS01", "DOC01", 3, "PKG", null, BigDecimal.valueOf(30), BigDecimal.valueOf(0.3), BigDecimal.valueOf(45));
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.of(master));
        given(houseBlPort.findConsoledAirSummariesByMasterBlId(id)).willReturn(List.of(a1));
        given(masterBlFactory.toDetailResult(any(), any(), any())).willReturn(mock(MasterBlDetailResult.class));

        masterBlService.findMasterBlById(id);

        then(houseBlPort).should().findConsoledAirSummariesByMasterBlId(id);
        then(houseBlPort).should(never()).findConsoledSeaSummariesByMasterBlId(any());
    }

    @Test
    @DisplayName("findMasterBlById: 없는 id → ResourceNotFoundException, houseBlPort 절대 미호출")
    void findMasterBlById_notFoundId_throwsResourceNotFoundExceptionAndSkipsHouseBlPort() {
        Long id = 999L;
        given(masterBlPort.findMasterBlById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.findMasterBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlPort).shouldHaveNoInteractions();
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
    @DisplayName("deleteMasterBlById - jobDiv projection empty 시 ResourceNotFoundException, deleteByIdAndJobDiv 미호출")
    void deleteMasterBlById_whenNotFound_throwsAndDoesNotDelete() {
        Long id = 999L;
        given(masterBlPort.findJobDivById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.deleteMasterBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(masterBlPort).should(never()).deleteByIdAndJobDiv(any(), any());
    }

    // ── searchMasterBls ───────────────────────────────────────────────

    @Test
    @DisplayName("searchMasterBls - SearchMasterBlCommand와 pageRequest를 port에 위임하고 결과를 그대로 반환")
    void searchMasterBls_delegatesToPort() {
        SearchMasterBlCommand cmd = new SearchMasterBlCommand("EXP", "MBL-001", null, null, null, null, null, null);
        MasterBlFilter filter = new MasterBlFilter(Bound.EXP, "MBL-001", null, null, null, null, null, null);
        PageRequest pageRequest = PageRequest.of(0, 20);
        MasterBlSummaryResult summary = new MasterBlSummaryResult(1L, "MBL-001", null, null, "EXP", null, null, null, null, null, null, null, null);
        PagedResult<MasterBlSummaryResult> portResult = PagedResult.of(List.of(summary), 1L, 1, 0, 20);
        given(masterBlFactory.toFilter(cmd)).willReturn(filter);
        given(masterBlPort.searchMasterBls(filter, pageRequest)).willReturn(portResult);

        PagedResult<MasterBlSummaryResult> result = masterBlService.searchMasterBls(cmd, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(masterBlPort).should().searchMasterBls(filter, pageRequest);
    }

    @Test
    @DisplayName("searchMasterBls - port PagedResult 메타(totalElements/totalPages/page/size) 그대로 반영")
    void searchMasterBls_returnsPagedResultWithCorrectMeta() {
        SearchMasterBlCommand cmd = new SearchMasterBlCommand("IMP", null, "SHIP01", null, "KRPUS", null, null, null);
        MasterBlFilter filter = new MasterBlFilter(Bound.IMP, null, "SHIP01", null, "KRPUS", null, null, null);
        PageRequest pageRequest = PageRequest.of(2, 15);
        MasterBlSummaryResult summary = new MasterBlSummaryResult(2L, "MBL-002", null, null, "IMP", null, null, null, null, null, null, null, null);
        PagedResult<MasterBlSummaryResult> portResult = PagedResult.of(List.of(summary), 50L, 4, 2, 15);
        given(masterBlFactory.toFilter(cmd)).willReturn(filter);
        given(masterBlPort.searchMasterBls(filter, pageRequest)).willReturn(portResult);

        PagedResult<MasterBlSummaryResult> result = masterBlService.searchMasterBls(cmd, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(50L);
        assertThat(result.getTotalPages()).isEqualTo(4);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(15);
    }
}
