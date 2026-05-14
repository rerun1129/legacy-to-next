package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.MblNo;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * HouseBlDomainToJpaMapper.applySeaCommonFields — conditional setter(PATCH 의미론) 검증.
 *
 * masterBlId: SEA form 미보유 필드 — setter를 두지 않아 DB 기존 값을 항상 보호한다 (§6.37).
 * mblNo/masterRefNo: 도메인 값이 null이면 setter skip(DB 기존 값 보호),
 *                    값이 있으면 setter 호출하여 UPDATE 반영한다 (toolbar 편집 경로).
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
    @DisplayName("applySeaCommonFields: mblNo null → setMblNo skip (DB 기존 값 보호 — PATCH 의미론)")
    void applySeaCommonFields_whenDomainMblNoNull_skipsSetMblNo() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);

        mapper.applySeaCommonFields(domain, spyJpa);

        verify(spyJpa, never()).setMblNo(any());
    }

    @Test
    @DisplayName("applySeaCommonFields: mblNo 있음 → setMblNo(값) 호출")
    void applySeaCommonFields_whenDomainMblNoPresent_callsSetMblNoWithValue() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        domain.assignMasterReference(MblNo.of("MBL12345"), null);

        mapper.applySeaCommonFields(domain, spyJpa);

        verify(spyJpa).setMblNo("MBL12345");
    }

    @Test
    @DisplayName("applySeaCommonFields: masterRefNo null → setMasterRefNo skip (DB 기존 값 보호 — PATCH 의미론)")
    void applySeaCommonFields_whenDomainMasterRefNoNull_skipsSetMasterRefNo() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);

        mapper.applySeaCommonFields(domain, spyJpa);

        verify(spyJpa, never()).setMasterRefNo(any());
    }

    @Test
    @DisplayName("applySeaCommonFields: masterRefNo 있음 → setMasterRefNo(값) 호출")
    void applySeaCommonFields_whenDomainMasterRefNoPresent_callsSetMasterRefNoWithValue() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        domain.assignMasterReference(null, "REF98765");

        mapper.applySeaCommonFields(domain, spyJpa);

        verify(spyJpa).setMasterRefNo("REF98765");
    }
}
