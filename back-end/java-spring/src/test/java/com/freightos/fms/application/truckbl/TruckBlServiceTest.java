package com.freightos.fms.application.truckbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.freight.port.out.FreightInputPort;
import com.freightos.fms.application.housebl.HouseBlFreightCommandBuilder;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.truckbl.port.out.TruckBlPersistencePort;
import com.freightos.fms.application.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.freight.enums.FreightBlType;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TruckBlServiceTest {

    @Mock private HouseBlPort houseBlPort;
    @Mock private TruckBlFactory truckBlFactory;
    @Mock private TruckBlPersistencePort truckBlPersistencePort;
    @Mock private TruckBlSearchPort truckBlSearchPort;
    @Mock private CodeNameResolver codeNameResolver;
    @Mock private FreightInputPort freightInputPort;
    @Mock private HouseBlFreightCommandBuilder houseBlFreightCommandBuilder;

    @InjectMocks
    private TruckBlService truckBlService;

    @Test
    @DisplayName("deleteTruckBlById: houseBlPort.deleteByIdAndJobDiv(id, TRUCK) 직접 호출 — HouseBlUseCase 우회")
    void deleteTruckBlById_callsPortDirectly() {
        given(freightInputPort.existsFreightLines(eq(FreightBlType.HOUSE), any())).willReturn(false);

        truckBlService.deleteTruckBlById(99L);

        then(houseBlPort).should().deleteByIdAndJobDiv(99L, JobDiv.TRUCK);
    }

    @Test
    @DisplayName("findTruckBlById: 미존재 id → ResourceNotFoundException")
    void findTruckBlById_notFound_throwsResourceNotFoundException() {
        given(houseBlPort.findHouseBlById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> truckBlService.findTruckBlById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findTruckBlById: jobDiv가 TRUCK이 아닌 행 → ResourceNotFoundException")
    void findTruckBlById_wrongJobDiv_throwsResourceNotFoundException() {
        // HouseBlSea 등 다른 타입 반환 시 instanceof HouseBlTruck 실패
        given(houseBlPort.findHouseBlById(10L)).willReturn(Optional.of(HouseBlSea.create(Bound.EXP)));

        assertThatThrownBy(() -> truckBlService.findTruckBlById(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("changeTruckBlHblNo: affected=0 → ResourceNotFoundException (jobDiv 불일치 or 미존재)")
    void changeTruckBlHblNo_zeroAffected_throwsResourceNotFoundException() {
        given(houseBlPort.updateHblNoById(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .willReturn(0L);

        assertThatThrownBy(() -> truckBlService.changeTruckBlHblNo(999L, new ChangeHouseBlNoCommand("NEW-001")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("changeTruckBlHblNo: hblNo blank → IllegalArgumentException (BlNumber.of 검증)")
    void changeTruckBlHblNo_blankHblNo_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> truckBlService.changeTruckBlHblNo(1L, new ChangeHouseBlNoCommand("")))
                .isInstanceOf(IllegalArgumentException.class);

        then(houseBlPort).should(never()).updateHblNoById(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("findTruckBlKeysByHblNoExact: truckBlSearchPort.findTruckBlKeysByHblNoExact 위임")
    void findTruckBlKeysByHblNoExact_delegatesToSearchPort() {
        given(truckBlSearchPort.findTruckBlKeysByHblNoExact("TB-001")).willReturn(List.of(1L, 2L));

        List<Long> result = truckBlService.findTruckBlKeysByHblNoExact("TB-001");

        then(truckBlSearchPort).should().findTruckBlKeysByHblNoExact("TB-001");
        Assertions.assertThat(result).containsExactly(1L, 2L);
    }
}
