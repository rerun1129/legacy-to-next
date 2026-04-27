package com.freightos.fms.application.masterbl;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.port.out.MasterBlPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MasterBlServiceTest {

    @Mock
    private MasterBlPort masterBlPort;

    @InjectMocks
    private MasterBlService masterBlService;

    @Test
    @DisplayName("list - Bound.EXP 조회 시 port.findAllByBound(EXP) 위임")
    void list_exp_delegatesToPort() {
        PageRequest pageRequest = PageRequest.of(0, 50);
        MasterBl mockEntity = mock(MasterBl.class);
        PagedResult<MasterBl> expected = PagedResult.of(List.of(mockEntity), 1L, 1, 0, 50);
        given(masterBlPort.findAllByBound(Bound.EXP, pageRequest)).willReturn(expected);

        PagedResult<MasterBl> result = masterBlService.list(Bound.EXP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(masterBlPort).should().findAllByBound(Bound.EXP, pageRequest);
    }

    @Test
    @DisplayName("list - Bound.IMP 조회 시 port.findAllByBound(IMP) 위임")
    void list_imp_delegatesToPort() {
        PageRequest pageRequest = PageRequest.of(0, 50);
        MasterBl mockEntity = mock(MasterBl.class);
        PagedResult<MasterBl> expected = PagedResult.of(List.of(mockEntity), 1L, 1, 0, 50);
        given(masterBlPort.findAllByBound(Bound.IMP, pageRequest)).willReturn(expected);

        PagedResult<MasterBl> result = masterBlService.list(Bound.IMP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(masterBlPort).should().findAllByBound(Bound.IMP, pageRequest);
    }

    @Test
    @DisplayName("getById - 존재하는 ID 조회 시 엔티티 반환")
    void getById_existingId_returnsEntity() {
        UUID id = UUID.randomUUID();
        MasterBl mockEntity = mock(MasterBl.class);
        given(masterBlPort.findById(id)).willReturn(Optional.of(mockEntity));

        MasterBl result = masterBlService.getById(id);

        assertThat(result).isEqualTo(mockEntity);
        then(masterBlPort).should().findById(id);
    }

    @Test
    @DisplayName("getById - 존재하지 않는 ID 조회 시 ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        given(masterBlPort.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete - 존재하는 ID 삭제 시 port.delete 호출")
    void delete_existingId_callsPortDelete() {
        UUID id = UUID.randomUUID();
        MasterBl mockEntity = mock(MasterBl.class);
        given(masterBlPort.findById(id)).willReturn(Optional.of(mockEntity));

        masterBlService.delete(id);

        then(masterBlPort).should().findById(id);
        then(masterBlPort).should().delete(mockEntity);
    }
}
