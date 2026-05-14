package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * HouseBlDomainToJpaMapper.applySeaCommonFields — SEA form 미보유 필드 setter 미호출 검증.
 *
 * SEA form에서 masterBlId/mblNo/masterRefNo를 직접 편집하는 경로가 없으므로,
 * applySeaCommonFields 호출 시 해당 setter가 JPA 엔티티에 절대 호출되지 않음을 보장한다.
 * setter 호출 시 DB 기존 값을 null로 덮어써 dirty-check이 발생하는 회귀(§6.37)를 방지.
 */
class HouseBlDomainToJpaMapperSeaTest {

    private HouseBlDomainToJpaMapper mapper;
    private HouseBlJpaEntity spyJpa;

    @BeforeEach
    void setUp() {
        mapper = new HouseBlDomainToJpaMapper();
        spyJpa = Mockito.spy(new HouseBlJpaEntity());
    }

    @Test
    @DisplayName("applySeaCommonFields: masterBlId setter 미호출 — DB 기존 값 보호")
    void applySeaCommonFields_doesNotCallSetMasterBlId() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);

        mapper.applySeaCommonFields(domain, spyJpa);

        verify(spyJpa, never()).setMasterBlId(Mockito.any());
    }

    @Test
    @DisplayName("applySeaCommonFields: mblNo setter 미호출 — DB 기존 값 보호")
    void applySeaCommonFields_doesNotCallSetMblNo() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);

        mapper.applySeaCommonFields(domain, spyJpa);

        verify(spyJpa, never()).setMblNo(Mockito.any());
    }

    @Test
    @DisplayName("applySeaCommonFields: masterRefNo setter 미호출 — DB 기존 값 보호")
    void applySeaCommonFields_doesNotCallSetMasterRefNo() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);

        mapper.applySeaCommonFields(domain, spyJpa);

        verify(spyJpa, never()).setMasterRefNo(Mockito.any());
    }
}
