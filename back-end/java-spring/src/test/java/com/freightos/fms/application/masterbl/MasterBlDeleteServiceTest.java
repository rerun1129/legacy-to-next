package com.freightos.fms.application.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.freight.port.out.FreightInputPort;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.masterbl.port.out.MasterBlPort;
import com.freightos.fms.application.masterbl.port.out.SeaMasterPersistencePort;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * deleteMasterBlById: nullifyMasterRefByMasterBlId → deleteByIdAndJobDiv 순서 보장 검증.
 * FK 위반 방지를 위해 자식 참조 해제가 부모 삭제보다 반드시 선행되어야 한다.
 */
@ExtendWith(MockitoExtension.class)
class MasterBlDeleteServiceTest {

    @Mock private MasterBlPort masterBlPort;
    @Mock private SeaMasterPersistencePort seaMasterPersistencePort;
    @Mock private HouseBlPort houseBlPort;
    @Mock private MasterBlFactory masterBlFactory;
    @Mock private FreightInputPort freightInputPort;
    @Mock private MasterBlFreightCommandBuilder masterBlFreightCommandBuilder;

    @InjectMocks
    private MasterBlService masterBlService;

    @Test
    @DisplayName("deleteMasterBlById - nullifyMasterRef가 deleteByIdAndJobDiv보다 먼저 호출된다")
    void deleteMasterBlById_nullifyCalledBeforeDelete() {
        Long id = 10L;
        given(masterBlPort.findJobDivById(id)).willReturn(Optional.of(MasterBlJobDiv.SEA));
        given(freightInputPort.existsFreightLines(any(), any())).willReturn(false);
        given(houseBlPort.nullifyMasterRefByMasterBlId(id)).willReturn(3);

        masterBlService.deleteMasterBlById(id);

        InOrder order = inOrder(freightInputPort, houseBlPort, masterBlPort);
        order.verify(freightInputPort).existsFreightLines(any(), any());
        order.verify(houseBlPort).nullifyMasterRefByMasterBlId(id);
        order.verify(masterBlPort).deleteByIdAndJobDiv(id, MasterBlJobDiv.SEA);
    }

    @Test
    @DisplayName("deleteMasterBlById - 연결된 HBL이 없을 때(0 rows)도 정상 삭제된다")
    void deleteMasterBlById_noLinkedHouseBl_stillDeletes() {
        Long id = 20L;
        given(masterBlPort.findJobDivById(id)).willReturn(Optional.of(MasterBlJobDiv.AIR));
        given(freightInputPort.existsFreightLines(any(), any())).willReturn(false);
        given(houseBlPort.nullifyMasterRefByMasterBlId(id)).willReturn(0);

        masterBlService.deleteMasterBlById(id);

        then(masterBlPort).should().deleteByIdAndJobDiv(id, MasterBlJobDiv.AIR);
    }

    @Test
    @DisplayName("deleteMasterBlById - jobDiv 조회 결과 없으면 ResourceNotFoundException, nullify/delete 모두 미호출")
    void deleteMasterBlById_notFound_throwsAndSkipsAll() {
        Long id = 999L;
        given(masterBlPort.findJobDivById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> masterBlService.deleteMasterBlById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlPort).should(never()).nullifyMasterRefByMasterBlId(id);
        then(masterBlPort).should(never()).deleteByIdAndJobDiv(id, MasterBlJobDiv.SEA);
    }
}
