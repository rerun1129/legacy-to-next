package com.freightos.fms.application.housebl;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

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
        HouseBl mockEntity = mock(HouseBl.class);
        PagedResult<HouseBl> portResult = PagedResult.of(List.of(mockEntity), 1L, 1, 0, 50);
        given(houseBlPort.findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv.SEA, Bound.EXP, pageRequest))
                .willReturn(portResult);

        PagedResult<HouseBl> result = houseBlService.getHouseBlsByJobDivAndBound(JobDiv.SEA, Bound.EXP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(houseBlPort).should()
                .findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv.SEA, Bound.EXP, pageRequest);
    }

    @Test
    @DisplayName("findHouseBlById - 존재하는 ID 조회 시 엔티티 반환")
    void findHouseBlById_existingId_returnsEntity() {
        Long id = 1L;
        HouseBl mockEntity = mock(HouseBl.class);
        given(houseBlPort.findById(id)).willReturn(Optional.of(mockEntity));

        HouseBl result = houseBlService.findHouseBlById(id);

        assertThat(result).isEqualTo(mockEntity);
        then(houseBlPort).should().findById(id);
    }

    @Test
    @DisplayName("findHouseBlById - 존재하지 않는 ID 조회 시 ResourceNotFoundException")
    void findHouseBlById_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(houseBlPort.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> houseBlService.findHouseBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteHouseBlById - 존재하는 ID 삭제 시 port.delete 호출")
    void deleteHouseBlById_existingId_callsPortDelete() {
        Long id = 1L;
        HouseBl mockEntity = mock(HouseBl.class);
        given(houseBlPort.findById(id)).willReturn(Optional.of(mockEntity));

        houseBlService.deleteHouseBlById(id);

        then(houseBlPort).should().findById(id);
        then(houseBlPort).should().delete(mockEntity);
    }
}
