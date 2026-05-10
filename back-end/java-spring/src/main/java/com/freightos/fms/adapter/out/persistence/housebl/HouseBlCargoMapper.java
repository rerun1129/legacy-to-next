package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.freightos.common.util.VoMapper.mapOrNull;

/**
 * House B/L 화물(컨테이너·DIM) JPA ↔ Domain 변환 매퍼.
 * 컨테이너는 SEA(house_bl_sea_container)/NON_BL(house_bl_nonbl_container) 테이블 분리.
 */
@Component
public class HouseBlCargoMapper {

    // ── SEA Container ────────────────────────────────────────────────

    public HouseBlSeaContainerJpaEntity toSeaContainerJpa(HouseBlContainer c) {
        HouseBlSeaContainerJpaEntity jpa = new HouseBlSeaContainerJpaEntity();
        if (c.getId() != null) jpa.setHouseBlSeaContainerId(c.getId());
        applySeaContainerFields(c, jpa);
        return jpa;
    }

    public HouseBlContainer toSeaContainerDomain(HouseBlSeaContainerJpaEntity jpa, HouseBl parent) {
        HouseBlContainer c = HouseBlContainer.of(parent, ContainerNumber.of(jpa.getContainerNo()),
                jpa.getContainerType(), jpa.getLengthFeet());
        c.assignIdentity(jpa.getHouseBlSeaContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(buildContainerDetails(
                jpa.getSealNo1(), jpa.getSealNo2(), jpa.getSealNo3(),
                jpa.getSealNo4(), jpa.getSealNo5(), jpa.getSealNo6(),
                jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getNetWeightKg(), jpa.getCbm(), jpa.getVgmKg(),
                jpa.isSoc(), jpa.getSeq()));
        return c;
    }

    // ── NON_BL Container ─────────────────────────────────────────────

    public HouseBlNonBlContainerJpaEntity toNonBlContainerJpa(HouseBlContainer c) {
        HouseBlNonBlContainerJpaEntity jpa = new HouseBlNonBlContainerJpaEntity();
        if (c.getId() != null) jpa.setHouseBlNonBlContainerId(c.getId());
        applyNonBlContainerFields(c, jpa);
        return jpa;
    }

    public HouseBlContainer toNonBlContainerDomain(HouseBlNonBlContainerJpaEntity jpa, HouseBl parent) {
        HouseBlContainer c = HouseBlContainer.of(parent, ContainerNumber.of(jpa.getContainerNo()),
                jpa.getContainerType(), jpa.getLengthFeet());
        c.assignIdentity(jpa.getHouseBlNonBlContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(buildContainerDetails(
                jpa.getSealNo1(), jpa.getSealNo2(), jpa.getSealNo3(),
                jpa.getSealNo4(), jpa.getSealNo5(), jpa.getSealNo6(),
                jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getNetWeightKg(), jpa.getCbm(), jpa.getVgmKg(),
                jpa.isSoc(), jpa.getSeq()));
        return c;
    }

    // ── E-12 DIM ──────────────────────────────────────────────────────

    public HouseBlDim toDimDomain(HouseBlDimJpaEntity jpa) {
        HouseBlDim domain = HouseBlDim.create(
                jpa.getHouseBlId(),
                jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm(),
                jpa.getQuantity(), jpa.getCbm(), jpa.getVolumeWeightKg());
        domain.assignIdentity(jpa.getHouseBlDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public List<HouseBlDim> toDimDomainList(List<HouseBlDimJpaEntity> jpaList) {
        return jpaList.stream().map(this::toDimDomain).collect(Collectors.toList());
    }

    public void applyDimFields(HouseBlDim domain, HouseBlDimJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setLengthCm(domain.getLengthCm());
        jpa.setWidthCm(domain.getWidthCm());
        jpa.setHeightCm(domain.getHeightCm());
        jpa.setQuantity(domain.getQuantity());
        jpa.setCbm(domain.getCbm());
        jpa.setVolumeWeightKg(domain.getVolumeWeightKg());
    }

    public HouseBlDimJpaEntity toDimJpa(HouseBlDim d, HouseBlJpaEntity houseBl) {
        HouseBlDimJpaEntity jpa = new HouseBlDimJpaEntity();
        if (d.getId() != null) jpa.setHouseBlDimId(d.getId());
        applyDimFields(d, jpa, houseBl);
        return jpa;
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────────

    private void applySeaContainerFields(HouseBlContainer c, HouseBlSeaContainerJpaEntity jpa) {
        jpa.setContainerNo(mapOrNull(c.getContainerNo(), ContainerNumber::value));
        jpa.setContainerType(c.getContainerType());
        jpa.setLengthFeet(c.getLengthFeet());
        jpa.setSealNo1(mapOrNull(c.getSealNo1(), SealNumber::value));
        jpa.setSealNo2(mapOrNull(c.getSealNo2(), SealNumber::value));
        jpa.setSealNo3(mapOrNull(c.getSealNo3(), SealNumber::value));
        jpa.setSealNo4(mapOrNull(c.getSealNo4(), SealNumber::value));
        jpa.setSealNo5(mapOrNull(c.getSealNo5(), SealNumber::value));
        jpa.setSealNo6(mapOrNull(c.getSealNo6(), SealNumber::value));
        jpa.setPkgQty(mapOrNull(c.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(c.getPkgUnit());
        jpa.setGrossWeightKg(mapOrNull(c.getGrossWeightKg(), Weight::kg));
        jpa.setNetWeightKg(mapOrNull(c.getNetWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(c.getCbm(), Volume::cbm));
        jpa.setVgmKg(mapOrNull(c.getVgmKg(), Weight::kg));
        jpa.setIsSoc(c.isSoc());
        jpa.setSeq(c.getSeq());
    }

    private void applyNonBlContainerFields(HouseBlContainer c, HouseBlNonBlContainerJpaEntity jpa) {
        jpa.setContainerNo(mapOrNull(c.getContainerNo(), ContainerNumber::value));
        jpa.setContainerType(c.getContainerType());
        jpa.setLengthFeet(c.getLengthFeet());
        jpa.setSealNo1(mapOrNull(c.getSealNo1(), SealNumber::value));
        jpa.setSealNo2(mapOrNull(c.getSealNo2(), SealNumber::value));
        jpa.setSealNo3(mapOrNull(c.getSealNo3(), SealNumber::value));
        jpa.setSealNo4(mapOrNull(c.getSealNo4(), SealNumber::value));
        jpa.setSealNo5(mapOrNull(c.getSealNo5(), SealNumber::value));
        jpa.setSealNo6(mapOrNull(c.getSealNo6(), SealNumber::value));
        jpa.setPkgQty(mapOrNull(c.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(c.getPkgUnit());
        jpa.setGrossWeightKg(mapOrNull(c.getGrossWeightKg(), Weight::kg));
        jpa.setNetWeightKg(mapOrNull(c.getNetWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(c.getCbm(), Volume::cbm));
        jpa.setVgmKg(mapOrNull(c.getVgmKg(), Weight::kg));
        jpa.setIsSoc(c.isSoc());
        jpa.setSeq(c.getSeq());
    }

    private HouseBlContainer.Details buildContainerDetails(
            String sealNo1, String sealNo2, String sealNo3, String sealNo4, String sealNo5, String sealNo6,
            Integer pkgQty, String pkgUnit,
            BigDecimal grossWeightKg, BigDecimal netWeightKg, BigDecimal cbm, BigDecimal vgmKg,
            boolean isSoc, int seq) {
        return new HouseBlContainer.Details(
                SealNumber.of(sealNo1), SealNumber.of(sealNo2), SealNumber.of(sealNo3),
                SealNumber.of(sealNo4), SealNumber.of(sealNo5), SealNumber.of(sealNo6),
                Quantity.of(pkgQty), pkgUnit,
                Weight.of(grossWeightKg), Weight.of(netWeightKg), Volume.of(cbm), Weight.of(vgmKg),
                isSoc, seq);
    }
}
