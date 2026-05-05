package com.freightos.fms.application.housebl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @InjectMocks
    private HouseBlService houseBlService;

    @Test
    @DisplayName("getHouseBlsByJobDivAndBound - jobDiv+bound 조건으로 port 위임 후 PagedResult 반환")
    void getHouseBlsByJobDivAndBound_delegatesToPort() {
        PageRequest pageRequest = PageRequest.of(0, 50);
        PageRequest sortedRequest = PageRequest.of(0, 50, "createdAt", SortDirection.DESC);
        HouseBlSummary mockSummary = mock(HouseBlSummary.class);
        PagedResult<HouseBlSummary> portResult = PagedResult.of(List.of(mockSummary), 1L, 1, 0, 50);
        given(houseBlPort.findHouseBlsByJobDivAndBound(JobDiv.SEA, Bound.EXP, sortedRequest))
                .willReturn(portResult);

        PagedResult<HouseBlSummary> result = houseBlService.getHouseBlsByJobDivAndBound(JobDiv.SEA, Bound.EXP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(houseBlPort).should()
                .findHouseBlsByJobDivAndBound(JobDiv.SEA, Bound.EXP, sortedRequest);
    }

    @Test
    @DisplayName("findHouseBlById - 존재하는 ID 조회 시 엔티티 반환")
    void findHouseBlById_existingId_returnsEntity() {
        Long id = 1L;
        HouseBl mockEntity = mock(HouseBl.class);
        given(houseBlPort.findHouseBlById(id)).willReturn(Optional.of(mockEntity));

        HouseBl result = houseBlService.findHouseBlById(id);

        assertThat(result).isEqualTo(mockEntity);
        then(houseBlPort).should().findHouseBlById(id);
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
    @DisplayName("save - port.saveHouseBl 위임 호출 후 port 반환값을 그대로 반환")
    void save_delegatesToPort_returnsResult() {
        HouseBl mockHouseBl = mock(HouseBl.class);
        given(houseBlPort.saveHouseBl(mockHouseBl)).willReturn(mockHouseBl);

        HouseBl result = houseBlService.save(mockHouseBl);

        assertThat(result).isEqualTo(mockHouseBl);
        then(houseBlPort).should().saveHouseBl(mockHouseBl);
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

    static Stream<Arguments> jobDivAndBoundCombinations() {
        return Stream.of(
                Arguments.of(JobDiv.SEA, Bound.IMP),
                Arguments.of(JobDiv.AIR, Bound.EXP),
                Arguments.of(JobDiv.AIR, Bound.IMP)
        );
    }

    @ParameterizedTest
    @MethodSource("jobDivAndBoundCombinations")
    @DisplayName("getHouseBlsByJobDivAndBound - 추가 JobDiv/Bound 조합에서 port 위임 및 sortBy=createdAt, DESC 검증")
    void getHouseBlsByJobDivAndBound_additionalEnumCombinations(JobDiv jobDiv, Bound bound) {
        PageRequest pageRequest = PageRequest.of(0, 20);
        PageRequest sortedRequest = PageRequest.of(0, 20, "createdAt", SortDirection.DESC);
        HouseBlSummary mockSummary = mock(HouseBlSummary.class);
        PagedResult<HouseBlSummary> portResult = PagedResult.of(List.of(mockSummary), 1L, 1, 0, 20);
        given(houseBlPort.findHouseBlsByJobDivAndBound(jobDiv, bound, sortedRequest))
                .willReturn(portResult);

        PagedResult<HouseBlSummary> result = houseBlService.getHouseBlsByJobDivAndBound(jobDiv, bound, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(houseBlPort).should()
                .findHouseBlsByJobDivAndBound(jobDiv, bound, sortedRequest);
        assertThat(sortedRequest.getSortBy()).isEqualTo("createdAt");
        assertThat(sortedRequest.getSortDirection()).isEqualTo(SortDirection.DESC);
    }

    @Test
    @DisplayName("getHouseBlsByJobDivAndBound - port PagedResult 메타(totalElements/totalPages/page/size) 그대로 반영")
    void getHouseBlsByJobDivAndBound_returnsPagedResultWithCorrectMeta() {
        PageRequest pageRequest = PageRequest.of(2, 10);
        PageRequest sortedRequest = PageRequest.of(2, 10, "createdAt", SortDirection.DESC);
        HouseBlSummary mockSummary = mock(HouseBlSummary.class);
        PagedResult<HouseBlSummary> portResult = PagedResult.of(List.of(mockSummary), 35L, 4, 2, 10);
        given(houseBlPort.findHouseBlsByJobDivAndBound(JobDiv.SEA, Bound.EXP, sortedRequest))
                .willReturn(portResult);

        PagedResult<HouseBlSummary> result = houseBlService.getHouseBlsByJobDivAndBound(JobDiv.SEA, Bound.EXP, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(35L);
        assertThat(result.getTotalPages()).isEqualTo(4);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(10);
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
}
