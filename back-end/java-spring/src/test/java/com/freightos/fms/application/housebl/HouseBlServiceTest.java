package com.freightos.fms.application.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @DisplayName("list - jobDiv+bound 조건으로 port 위임 후 DTO 변환")
    void list_delegatesToPort() {
        Pageable pageable = PageRequest.of(0, 50);
        HouseBl mockEntity = mock(HouseBl.class);
        Page<HouseBl> portResult = new PageImpl<>(List.of(mockEntity));
        given(houseBlPort.findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv.SEA, Bound.EXP, pageable))
                .willReturn(portResult);

        Page<HouseBlSummaryResponse> result = houseBlService.list(JobDiv.SEA, Bound.EXP, pageable);

        assertThat(result).hasSize(1);
        then(houseBlPort).should().findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv.SEA, Bound.EXP, pageable);
    }

    @Test
    @DisplayName("getById - 존재하는 ID 조회 시 엔티티 반환")
    void getById_existingId_returnsEntity() {
        UUID id = UUID.randomUUID();
        HouseBl mockEntity = mock(HouseBl.class);
        given(houseBlPort.findById(id)).willReturn(Optional.of(mockEntity));

        HouseBl result = houseBlService.getById(id);

        assertThat(result).isEqualTo(mockEntity);
        then(houseBlPort).should().findById(id);
    }

    @Test
    @DisplayName("getById - 존재하지 않는 ID 조회 시 ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        given(houseBlPort.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> houseBlService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete - 존재하는 ID 삭제 시 port.delete 호출")
    void delete_existingId_callsPortDelete() {
        UUID id = UUID.randomUUID();
        HouseBl mockEntity = mock(HouseBl.class);
        given(houseBlPort.findById(id)).willReturn(Optional.of(mockEntity));

        houseBlService.delete(id);

        then(houseBlPort).should().findById(id);
        then(houseBlPort).should().delete(mockEntity);
    }
}
