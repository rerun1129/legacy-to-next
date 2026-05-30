package com.freightos.fms.application.truckbl;

import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.truckbl.port.out.TruckBlPersistencePort;
import com.freightos.fms.application.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailView;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

/**
 * TruckBlService.findTruckBlById의 hsCode 로드 + hsCodeName resolve 동작 검증.
 * TruckBlFactory는 실제 구현(real)을 사용하여 end-to-end 매핑까지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class TruckBlServiceEnrichTest {

    @Mock private HouseBlPort houseBlPort;
    @Mock private TruckBlPersistencePort truckBlPersistencePort;
    @Mock private TruckBlSearchPort truckBlSearchPort;
    @Mock private CodeNameResolver codeNameResolver;
    @Mock private HouseBlFactory houseBlFactory;

    private TruckBlService truckBlService;

    @BeforeEach
    void setUp() {
        TruckBlFactory truckBlFactory = new TruckBlFactory(houseBlFactory);
        truckBlService = new TruckBlService(
                houseBlPort, truckBlFactory, truckBlPersistencePort, truckBlSearchPort, codeNameResolver);
    }

    @Test
    @DisplayName("findTruckBlById: hsCode 존재 시 codeNameResolver로 hsCodeName을 resolve한다")
    void findTruckBlById_withHsCode_resolvesHsCodeName() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        truck.updateTradeInfo(null, null, null, "8471.30");
        given(houseBlPort.findHouseBlById(1L)).willReturn(Optional.of(truck));
        given(codeNameResolver.findHsCodeNames(anyCollection()))
                .willReturn(Map.of("8471.30", "자동 데이터 처리 기계"));

        TruckBlDetailView view = truckBlService.findTruckBlById(1L);

        assertThat(view.base().hsCode()).isEqualTo("8471.30");
        assertThat(view.hsCodeName()).isEqualTo("자동 데이터 처리 기계");
    }

    @Test
    @DisplayName("findTruckBlById: hsCode null 시 빈 Set으로 findHsCodeNames 호출 → hsCodeName 빈 문자열")
    void findTruckBlById_withNullHsCode_returnsEmptyHsCodeName() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        // hsCode 미설정 → null
        given(houseBlPort.findHouseBlById(2L)).willReturn(Optional.of(truck));
        given(codeNameResolver.findHsCodeNames(Set.of())).willReturn(Map.of());

        TruckBlDetailView view = truckBlService.findTruckBlById(2L);

        assertThat(view.base().hsCode()).isNull();
        assertThat(view.hsCodeName()).isEmpty();
    }

    @Test
    @DisplayName("findTruckBlById: codeNameResolver에 없는 hsCode → hsCodeName 빈 문자열")
    void findTruckBlById_hsCodeNotInResolver_returnsEmptyHsCodeName() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        truck.updateTradeInfo(null, null, null, "9999.99");
        given(houseBlPort.findHouseBlById(3L)).willReturn(Optional.of(truck));
        given(codeNameResolver.findHsCodeNames(anyCollection())).willReturn(Map.of());

        TruckBlDetailView view = truckBlService.findTruckBlById(3L);

        assertThat(view.base().hsCode()).isEqualTo("9999.99");
        assertThat(view.hsCodeName()).isEmpty();
    }
}
