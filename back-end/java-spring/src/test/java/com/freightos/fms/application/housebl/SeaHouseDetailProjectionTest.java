package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.application.housebl.projection.SeaContainerProjection;
import com.freightos.fms.application.housebl.projection.SeaDescProjection;
import com.freightos.fms.application.housebl.projection.SeaDetailProjection;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HouseBlFactory.toDetailResult — SEA 자식 컬렉션(containers·desc) projection 변환 검증.
 * HouseBlFactory는 순수 Spring Component이므로 @ExtendWith 없이 직접 생성.
 */
class SeaHouseDetailProjectionTest {

    private HouseBlFactory sut;

    @BeforeEach
    void setUp() {
        sut = new HouseBlFactory(
                new HouseBlSubFactory(),
                new HouseBlSeaSubFactory(),
                new HouseBlTruckSubFactory(),
                new HouseBlNonBlSubFactory(),
                new HouseBlAirSubFactory(),
                new HouseBlSeaDetailSubFactory(),
                new HouseBlAirDetailSubFactory()
        );
    }

    @Test
    @DisplayName("SEA 엔티티에 컨테이너 1건 있을 때 seaDetail.containers size=1, 필드 일치")
    void toDetailResult_seaWithOneContainer_containersExposedInProjection() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);

        HouseBlContainer container = HouseBlContainer.of(sea, ContainerNumber.of("CSQU3054383"), ContainerType.T20GP, 20);
        container.updateDetails(new HouseBlContainer.Details(
                null, null, null, null, null, null,
                Quantity.of(10), "CTN",
                Weight.of(BigDecimal.valueOf(1000)),
                Weight.of(BigDecimal.valueOf(900)),
                Volume.of(BigDecimal.valueOf(25)),
                null, false, 1
        ));
        sea.initContainers(List.of(container));

        HouseBlDetailResult result = sut.toDetailResult(sea);

        assertThat(result.seaDetail()).isNotNull();
        SeaDetailProjection seaDetail = result.seaDetail();
        assertThat(seaDetail.containers()).hasSize(1);

        SeaContainerProjection c = seaDetail.containers().get(0);
        assertThat(c.containerNo()).isEqualTo("CSQU3054383");
        assertThat(c.containerType()).isEqualTo(ContainerType.T20GP.name());
        assertThat(c.lengthFeet()).isEqualTo(20);
        assertThat(c.pkgQty()).isEqualTo(10);
        assertThat(c.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(c.cbm()).isEqualByComparingTo(BigDecimal.valueOf(25));
        assertThat(c.seq()).isEqualTo(1);
    }

    @Test
    @DisplayName("SEA 엔티티에 컨테이너 없을 때 seaDetail.containers 빈 리스트")
    void toDetailResult_seaWithNoContainers_emptyContainerList() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.initContainers(List.of());

        HouseBlDetailResult result = sut.toDetailResult(sea);

        assertThat(result.seaDetail()).isNotNull();
        assertThat(result.seaDetail().containers()).isEmpty();
    }

    @Test
    @DisplayName("SEA 엔티티에 desc 있을 때 seaDetail.desc 필드 일치")
    void toDetailResult_seaWithDesc_descExposedInProjection() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.initContainers(List.of());

        HouseBlDesc desc = HouseBlDesc.create(null);
        desc.updateContent("MARKS-001", "DESCRIPTION-001", DescClause1.A, DescClause2.A);
        sea.initDesc(desc);

        HouseBlDetailResult result = sut.toDetailResult(sea);

        assertThat(result.seaDetail()).isNotNull();
        SeaDescProjection seaDesc = result.seaDetail().desc();
        assertThat(seaDesc).isNotNull();
        assertThat(seaDesc.marks()).isEqualTo("MARKS-001");
        assertThat(seaDesc.description()).isEqualTo("DESCRIPTION-001");
        assertThat(seaDesc.descClause1()).isEqualTo(DescClause1.A.name());
        assertThat(seaDesc.descClause2()).isEqualTo(DescClause2.A.name());
    }

    @Test
    @DisplayName("SEA 엔티티에 desc null일 때 seaDetail.desc null")
    void toDetailResult_seaWithNullDesc_descIsNull() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.initContainers(List.of());

        HouseBlDetailResult result = sut.toDetailResult(sea);

        assertThat(result.seaDetail()).isNotNull();
        assertThat(result.seaDetail().desc()).isNull();
    }

    @Test
    @DisplayName("incoterms·salesClass 설정 시 DetailResult에 enum name()으로 노출된다")
    void toDetailResult_withIncotermsAndSalesClass_exposedAsEnumName() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.updateTradeInfo(Incoterms.FOB, SalesClass.S, null, null);
        sea.initContainers(List.of());

        HouseBlDetailResult result = sut.toDetailResult(sea);

        assertThat(result.incoterms()).isEqualTo(Incoterms.FOB.name());
        assertThat(result.salesClass()).isEqualTo(SalesClass.S.name());
    }

    @Test
    @DisplayName("incoterms·salesClass null 시 DetailResult에 null로 노출된다")
    void toDetailResult_withNullIncotermsAndSalesClass_exposedAsNull() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.initContainers(List.of());

        HouseBlDetailResult result = sut.toDetailResult(sea);

        assertThat(result.incoterms()).isNull();
        assertThat(result.salesClass()).isNull();
    }
}
