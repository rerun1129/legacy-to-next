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
        given(masterBlPort.getMasterBlsByBound(Bound.EXP, pageRequest)).willReturn(expected);

        PagedResult<MasterBl> result = masterBlService.getMasterBlsByBound(Bound.EXP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(masterBlPort).should().getMasterBlsByBound(Bound.EXP, pageRequest);
    }

    @Test
    @DisplayName("list - Bound.IMP 조회 시 port.findAllByBound(IMP) 위임")
    void list_imp_delegatesToPort() {
        PageRequest pageRequest = PageRequest.of(0, 50);
        MasterBl mockEntity = mock(MasterBl.class);
        PagedResult<MasterBl> expected = PagedResult.of(List.of(mockEntity), 1L, 1, 0, 50);
        given(masterBlPort.getMasterBlsByBound(Bound.IMP, pageRequest)).willReturn(expected);

        PagedResult<MasterBl> result = masterBlService.getMasterBlsByBound(Bound.IMP, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        then(masterBlPort).should().getMasterBlsByBound(Bound.IMP, pageRequest);
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
}
