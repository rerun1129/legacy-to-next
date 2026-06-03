package com.freightos.fms.application.nonbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.common.model.PageRequest;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.freight.port.out.FreightInputPort;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.HouseBlFreightCommandBuilder;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.nonbl.port.out.NonBlPersistencePort;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlDetailView;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.freight.enums.FreightBlType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NonBlServiceTest {

    @Mock private HouseBlUseCase houseBlUseCase;
    @Mock private HouseBlPort houseBlPort;
    @Mock private HouseBlFactory houseBlFactory;
    @Mock private NonBlPersistencePort nonBlPersistencePort;
    @Mock private NonBlSearchPort nonBlSearchPort;
    @Mock private CodeNameResolver codeNameResolver;
    @Mock private FreightInputPort freightInputPort;
    @Mock private HouseBlFreightCommandBuilder houseBlFreightCommandBuilder;

    @InjectMocks
    private NonBlService nonBlService;

    @Test
    @DisplayName("deleteNonBlById: houseBlPort.deleteByIdAndJobDiv(id, NON_BL) 직접 호출 — houseBlUseCase 우회")
    void deleteNonBlById_callsPortDirectly_bypassesHouseBlUseCase() {
        given(freightInputPort.existsFreightLines(eq(FreightBlType.HOUSE), any())).willReturn(false);

        nonBlService.deleteNonBlById(99L);

        then(houseBlPort).should().deleteByIdAndJobDiv(99L, JobDiv.NON_BL);
        then(houseBlUseCase).should(never()).deleteHouseBlById(any());
    }

    @Test
    @DisplayName("findNonBlById: nonBlSearchPort.findNonBlById 호출, ResourceNotFoundException on empty")
    void findNonBlById_notFound_throwsResourceNotFoundException() {
        given(nonBlSearchPort.findNonBlById(123L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> nonBlService.findNonBlById(123L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findNonBlById: 존재하는 경우 NonBlDetailView 반환 — hsCodeName resolved, freight 조회 포함")
    void findNonBlById_found_returnsDetailResult() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        given(nonBlSearchPort.findNonBlById(1L)).willReturn(Optional.of(domain));
        given(codeNameResolver.findHsCodeNames(any(Collection.class))).willReturn(Map.of());
        given(freightInputPort.findFreightByBl(eq(FreightBlType.HOUSE), any())).willReturn(Optional.empty());

        NonBlDetailView view = nonBlService.findNonBlById(1L);

        then(nonBlSearchPort).should().findNonBlById(1L);
        then(codeNameResolver).should().findHsCodeNames(any(Collection.class));
        then(freightInputPort).should().findFreightByBl(eq(FreightBlType.HOUSE), any());
        assertThat(view.base()).isNotNull();
        assertThat(view.hsCodeName()).isNotNull();
        assertThat(view.freight()).isNull();
    }

    @Test
    @DisplayName("deleteNonBlById: freight 라인 없으면 freight 삭제 후 houseBlPort.deleteByIdAndJobDiv 호출")
    void deleteNonBlById_noFreightLines_deletesSuccessfully() {
        given(freightInputPort.existsFreightLines(eq(FreightBlType.HOUSE), any())).willReturn(false);

        nonBlService.deleteNonBlById(99L);

        then(freightInputPort).should().deleteFreight(eq(FreightBlType.HOUSE), any());
        then(houseBlPort).should().deleteByIdAndJobDiv(99L, JobDiv.NON_BL);
        then(houseBlUseCase).should(never()).deleteHouseBlById(any());
    }
}
