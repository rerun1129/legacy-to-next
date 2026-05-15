package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * MasterBlSeaSubMapper — conditional setter(PATCH 의미론, §6.37) 검증.
 *
 * <ul>
 *   <li>form 편집 가능 필드(mblNo·masterRefNo·shipperCode 등): 도메인 값 null → setter skip, 값 있으면 setter 호출
 *   <li>form 미보유(masterBlId 자기 PK): setter 없으므로 항상 skip
 * </ul>
 *
 * House {@code HouseBlDomainToJpaMapperSeaTest} 동등 패턴.
 */
class MasterBlSeaSubMapperTest {

    private MasterBlSeaSubMapper mapper;
    private MasterBlJpaEntity spyParentJpa;
    private MasterBlSeaJpaEntity spySeaJpa;

    @BeforeEach
    void setUp() {
        mapper = new MasterBlSeaSubMapper();
        spyParentJpa = Mockito.spy(new MasterBlJpaEntity());
        spySeaJpa = Mockito.spy(new MasterBlSeaJpaEntity());
    }

    // ── applyMasterSeaCommonFields: mblNo conditional setter 검증 ─────

    @Test
    @DisplayName("applyMasterSeaCommonFields: mblNo null → setMblNo skip (DB 기존 값 보호 — PATCH 의미론)")
    void applyMasterSeaCommonFields_whenMblNoNull_skipsMblNo() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP); // mblNo null

        mapper.applyMasterSeaCommonFields(domain, spyParentJpa);

        verify(spyParentJpa, never()).setMblNo(any());
    }

    @Test
    @DisplayName("applyMasterSeaCommonFields: mblNo 있음 → setMblNo(값) 호출")
    void applyMasterSeaCommonFields_whenMblNoPresent_callsSetMblNo() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);
        domain.assignMblNo(BlNumber.of("MBL12345"), null);

        mapper.applyMasterSeaCommonFields(domain, spyParentJpa);

        verify(spyParentJpa).setMblNo("MBL12345");
    }

    // ── applyMasterSeaCommonFields: masterRefNo conditional setter 검증 ─

    @Test
    @DisplayName("applyMasterSeaCommonFields: masterRefNo null → setMasterRefNo skip")
    void applyMasterSeaCommonFields_whenMasterRefNoNull_skipsMasterRefNo() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);

        mapper.applyMasterSeaCommonFields(domain, spyParentJpa);

        verify(spyParentJpa, never()).setMasterRefNo(any());
    }

    @Test
    @DisplayName("applyMasterSeaCommonFields: masterRefNo 있음 → setMasterRefNo(값) 호출")
    void applyMasterSeaCommonFields_whenMasterRefNoPresent_callsSetMasterRefNo() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);
        domain.assignMblNo(null, BlNumber.of("REF98765"));

        mapper.applyMasterSeaCommonFields(domain, spyParentJpa);

        verify(spyParentJpa).setMasterRefNo("REF98765");
    }

    // ── applyMasterSeaCommonFields: shipperCode null → setShipperCode skip ─

    @Test
    @DisplayName("applyMasterSeaCommonFields: shipperCode null → setShipperCode·setShipperAddress skip")
    void applyMasterSeaCommonFields_whenShipperCodeNull_skipsShipperFields() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP); // shipperCode null

        mapper.applyMasterSeaCommonFields(domain, spyParentJpa);

        verify(spyParentJpa, never()).setShipperCode(any());
        verify(spyParentJpa, never()).setShipperAddress(any());
    }

    @Test
    @DisplayName("applyMasterSeaCommonFields: shipperCode 있음 → setShipperCode·setShipperAddress 호출")
    void applyMasterSeaCommonFields_whenShipperCodePresent_callsShipperFields() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);
        domain.assignParties(CustomerCode.of("SHIPPER01"), null, null);

        mapper.applyMasterSeaCommonFields(domain, spyParentJpa);

        verify(spyParentJpa).setShipperCode("SHIPPER01");
        verify(spyParentJpa).setShipperAddress(null); // address 없으면 null 전달
    }

    // ── applyMasterSeaFields: loadType conditional setter 검증 ───────

    @Test
    @DisplayName("applyMasterSeaFields: loadType null → setLoadType skip")
    void applyMasterSeaFields_whenLoadTypeNull_skipsLoadType() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);

        mapper.applyMasterSeaFields(domain, spySeaJpa);

        verify(spySeaJpa, never()).setLoadType(any());
    }

    @Test
    @DisplayName("applyMasterSeaFields: loadType 있음 → setLoadType(값) 호출")
    void applyMasterSeaFields_whenLoadTypePresent_callsSetLoadType() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);
        domain.updateSeaFields(
                com.freightos.fms.domain.common.enums.LoadType.FCL,
                null, null, null, null, null
        );

        mapper.applyMasterSeaFields(domain, spySeaJpa);

        verify(spySeaJpa).setLoadType(com.freightos.fms.domain.common.enums.LoadType.FCL);
    }
}
