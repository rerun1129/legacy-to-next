package com.freightos.fms.application.truckbl;

import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.entity.HouseBlTruckOrder;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.TruckType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TruckBlFactoryTest {

    @Mock
    private HouseBlFactory houseBlFactory;

    private TruckBlFactory truckBlFactory;

    @BeforeEach
    void setUp() {
        truckBlFactory = new TruckBlFactory(houseBlFactory);
    }

    @Test
    @DisplayName("toDetailResult: truckOrders가 비어 있으면 빈 리스트를 반환한다")
    void toDetailResult_emptyTruckOrders_returnsEmptyList() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);

        TruckBlDetailResult result = truckBlFactory.toDetailResult(truck);

        assertThat(result.truckOrders()).isEmpty();
    }

    @Test
    @DisplayName("toDetailResult: desc가 null이면 DescView도 null을 반환한다")
    void toDetailResult_nullDesc_returnsNullDescView() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);

        TruckBlDetailResult result = truckBlFactory.toDetailResult(truck);

        assertThat(result.desc()).isNull();
    }

    @Test
    @DisplayName("toDetailResult: TruckOrder 필드가 모두 올바르게 매핑된다")
    void toDetailResult_truckOrderFields_mappedCorrectly() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        truck.assignIdentity(1L, null, null, null, null);

        HouseBlTruckOrder order = HouseBlTruckOrder.create(1L);
        order.assignIdentity(10L, null, null, null, null);
        order.updateDetails(new HouseBlTruckOrder.Details(
                "ORD-001",
                3,
                "CTN",
                Weight.of(new BigDecimal("100.5")),
                Volume.of(new BigDecimal("2.3")),
                "TRK-001",
                TruckType.T50,
                "홍길동",
                "010-1234-5678",
                ContainerNumber.of("ABCD1234567"),
                ContainerType.T20GP,
                SealNumber.of("SEAL-1"),
                SealNumber.of("SEAL-2"),
                SealNumber.of("SEAL-3")
        ));
        truck.initTruckOrders(List.of(order));

        TruckBlDetailResult result = truckBlFactory.toDetailResult(truck);

        assertThat(result.truckOrders()).hasSize(1);
        TruckBlDetailResult.TruckOrderView view = result.truckOrders().get(0);
        assertThat(view.id()).isEqualTo(10L);
        assertThat(view.truckOrderNo()).isEqualTo("ORD-001");
        assertThat(view.pkgQty()).isEqualTo(3);
        assertThat(view.pkgUnit()).isEqualTo("CTN");
        assertThat(view.grossWeightKg()).isEqualByComparingTo(new BigDecimal("100.5"));
        assertThat(view.cbm()).isEqualByComparingTo(new BigDecimal("2.3"));
        assertThat(view.truckNo()).isEqualTo("TRK-001");
        assertThat(view.truckType()).isEqualTo("T50");
        assertThat(view.driver()).isEqualTo("홍길동");
        assertThat(view.mobileNo()).isEqualTo("010-1234-5678");
        assertThat(view.containerNo()).isEqualTo("ABCD1234567");
        assertThat(view.containerType()).isEqualTo("20GP");
        assertThat(view.sealNo1()).isEqualTo("SEAL-1");
        assertThat(view.sealNo2()).isEqualTo("SEAL-2");
        assertThat(view.sealNo3()).isEqualTo("SEAL-3");
    }

    @Test
    @DisplayName("toDetailResult: TruckOrder VO 필드가 null이면 null로 매핑된다")
    void toDetailResult_truckOrderNullVoFields_mappedAsNull() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        truck.assignIdentity(1L, null, null, null, null);

        HouseBlTruckOrder order = HouseBlTruckOrder.create(1L);
        order.assignIdentity(20L, null, null, null, null);
        order.updateDetails(new HouseBlTruckOrder.Details(
                "ORD-002", null, null, null, null,
                null, null, null, null, null, null, null, null, null
        ));
        truck.initTruckOrders(List.of(order));

        TruckBlDetailResult result = truckBlFactory.toDetailResult(truck);

        TruckBlDetailResult.TruckOrderView view = result.truckOrders().get(0);
        assertThat(view.grossWeightKg()).isNull();
        assertThat(view.cbm()).isNull();
        assertThat(view.truckType()).isNull();
        assertThat(view.containerNo()).isNull();
        assertThat(view.containerType()).isNull();
        assertThat(view.sealNo1()).isNull();
        assertThat(view.sealNo2()).isNull();
        assertThat(view.sealNo3()).isNull();
    }

    @Test
    @DisplayName("toDetailResult: HouseBlDesc 필드가 올바르게 DescView로 매핑된다")
    void toDetailResult_descFields_mappedCorrectly() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        truck.assignIdentity(1L, null, null, null, null);

        HouseBlDesc desc = HouseBlDesc.create(1L);
        desc.updateContent("MARK-001", "화물 설명", DescClause1.A, DescClause2.fromCode("A"));
        truck.initDesc(desc);
        truck.updateRemark("비고");

        TruckBlDetailResult result = truckBlFactory.toDetailResult(truck);

        assertThat(result.desc()).isNotNull();
        assertThat(result.desc().marks()).isEqualTo("MARK-001");
        assertThat(result.desc().description()).isEqualTo("화물 설명");
        assertThat(result.desc().descClause1()).isEqualTo("A");
        assertThat(result.desc().descClause2()).isNotNull();
        assertThat(result.remark()).isEqualTo("비고");
    }
}
