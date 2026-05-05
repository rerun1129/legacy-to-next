package com.freightos.fms.application.housebl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

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
class HouseBlServiceTest {

    @Mock
    private HouseBlPort houseBlPort;

    @Mock
    private HouseBlFactory houseBlFactory;

    @InjectMocks
    private HouseBlService houseBlService;

    @Test
    @DisplayName("findHouseBlById - 존재하는 ID 조회 시 HouseBlDetailResult 반환")
    void findHouseBlById_existingId_returnsDetailResult() {
        Long id = 1L;
        HouseBl mockEntity = mock(HouseBl.class);
        HouseBlDetailResult mockResult = mock(HouseBlDetailResult.class);
        given(houseBlPort.findHouseBlById(id)).willReturn(Optional.of(mockEntity));
        given(houseBlFactory.toDetailResult(mockEntity)).willReturn(mockResult);

        HouseBlDetailResult result = houseBlService.findHouseBlById(id);

        assertThat(result).isEqualTo(mockResult);
        then(houseBlPort).should().findHouseBlById(id);
        then(houseBlFactory).should().toDetailResult(mockEntity);
    }

    @Test
    @DisplayName("findHouseBlById - 존재하지 않는 ID 조회 시 ResourceNotFoundException")
    void findHouseBlById_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(houseBlPort.findHouseBlById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> houseBlService.findHouseBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteHouseBlById - 존재하는 ID 삭제 시 port.delete 호출")
    void deleteHouseBlById_existingId_callsPortDelete() {
        Long id = 1L;
        HouseBl mockEntity = mock(HouseBl.class);
        given(houseBlPort.findHouseBlById(id)).willReturn(Optional.of(mockEntity));

        houseBlService.deleteHouseBlById(id);

        then(houseBlPort).should().findHouseBlById(id);
        then(houseBlPort).should().deleteHouseBl(mockEntity);
    }

    @Test
    @DisplayName("createHouseBl - factory로 엔티티 생성 후 port.saveHouseBl 위임 호출, 저장된 엔티티의 ID를 반환")
    void createHouseBl_delegatesToPort_returnsId() {
        Long expectedId = 1L;
        CreateHouseBlCommand mockCommand = mock(CreateHouseBlCommand.class);
        HouseBl mockEntity = mock(HouseBl.class);
        HouseBl mockSaved = mock(HouseBl.class);
        given(houseBlFactory.toEntity(mockCommand)).willReturn(mockEntity);
        given(mockSaved.getId()).willReturn(expectedId);
        given(houseBlPort.saveHouseBl(mockEntity)).willReturn(mockSaved);

        Long result = houseBlService.createHouseBl(mockCommand);

        assertThat(result).isEqualTo(expectedId);
        then(houseBlFactory).should().toEntity(mockCommand);
        then(houseBlPort).should().saveHouseBl(mockEntity);
    }

    @Test
    @DisplayName("deleteHouseBlById - 없는 ID 조회 시 ResourceNotFoundException throw, port.delete 미호출")
    void deleteHouseBlById_whenNotFound_throwsAndDoesNotDelete() {
        Long id = 999L;
        given(houseBlPort.findHouseBlById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> houseBlService.deleteHouseBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlPort).should(never()).deleteHouseBl(any());
    }

    @Test
    @DisplayName("HouseBlService 클래스 레벨에 @Transactional(readOnly=true)가 선언되어 있다")
    void houseBlServiceClass_isAnnotatedWithTransactionalReadOnlyTrue() {
        Transactional annotation = HouseBlService.class.getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }

    // ── searchHouseBls ────────────────────────────────────────

    @Test
    @DisplayName("searchHouseBls - filter와 pageRequest를 port에 위임하고 결과를 그대로 반환")
    void searchHouseBls_delegatesToPort() {
        HouseBlFilter filter = HouseBlFilter.of(JobDiv.SEA, Bound.EXP, "HBL-001", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        PageRequest pageRequest = PageRequest.of(0, 20);
        HouseBlSummary mockSummary = mock(HouseBlSummary.class);
        PagedResult<HouseBlSummary> portResult = PagedResult.of(List.of(mockSummary), 1L, 1, 0, 20);
        given(houseBlPort.searchHouseBls(filter, pageRequest)).willReturn(portResult);

        PagedResult<HouseBlSummary> result = houseBlService.searchHouseBls(filter, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(houseBlPort).should().searchHouseBls(filter, pageRequest);
    }

    @Test
    @DisplayName("searchHouseBls - port PagedResult 메타(totalElements/totalPages/page/size) 그대로 반영")
    void searchHouseBls_returnsPagedResultWithCorrectMeta() {
        HouseBlFilter filter = HouseBlFilter.of(JobDiv.AIR, Bound.IMP, null, null, "SHIP01", null, null, null, null, null, null, null, null, null, null, null, null);
        PageRequest pageRequest = PageRequest.of(1, 10);
        HouseBlSummary mockSummary = mock(HouseBlSummary.class);
        PagedResult<HouseBlSummary> portResult = PagedResult.of(List.of(mockSummary), 25L, 3, 1, 10);
        given(houseBlPort.searchHouseBls(filter, pageRequest)).willReturn(portResult);

        PagedResult<HouseBlSummary> result = houseBlService.searchHouseBls(filter, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(25L);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }

    // ── updateHouseBl ────────────────────────────────────────

    @Test
    @DisplayName("updateHouseBl - factory.applyToEntity가 기존 엔티티에 호출됨을 검증")
    void updateHouseBl_factoryApplyToEntityIsCalled() {
        Long id = 1L;
        UpdateHouseBlCommand mockCommand = mock(UpdateHouseBlCommand.class);
        HouseBl mockEntity = mock(HouseBl.class);
        given(houseBlPort.findHouseBlById(id)).willReturn(Optional.of(mockEntity));
        given(houseBlPort.saveHouseBl(mockEntity)).willReturn(mockEntity);
        given(houseBlFactory.toDetailResult(mockEntity)).willReturn(mock(HouseBlDetailResult.class));

        houseBlService.updateHouseBl(id, mockCommand);

        then(houseBlFactory).should().applyToEntity(mockCommand, mockEntity);
    }

    @Test
    @DisplayName("updateHouseBl - factory 적용 후 port.saveHouseBl 위임 및 HouseBlDetailResult 반환 검증")
    void updateHouseBl_delegatesToPortSave() {
        Long id = 1L;
        UpdateHouseBlCommand mockCommand = mock(UpdateHouseBlCommand.class);
        HouseBl mockEntity = mock(HouseBl.class);
        HouseBlDetailResult mockResult = mock(HouseBlDetailResult.class);
        given(houseBlPort.findHouseBlById(id)).willReturn(Optional.of(mockEntity));
        given(houseBlPort.saveHouseBl(mockEntity)).willReturn(mockEntity);
        given(houseBlFactory.toDetailResult(mockEntity)).willReturn(mockResult);

        HouseBlDetailResult result = houseBlService.updateHouseBl(id, mockCommand);

        assertThat(result).isEqualTo(mockResult);
        then(houseBlPort).should().saveHouseBl(mockEntity);
        then(houseBlFactory).should().toDetailResult(mockEntity);
    }
}
