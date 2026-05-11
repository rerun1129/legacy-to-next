package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlDimJpaEntity;
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

    public HouseBlDim toAirDimDomain(HouseBlAirDimJpaEntity jpa) {
        HouseBlDim domain = HouseBlDim.create(
                jpa.getHouseBlAirId(),
                jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm(),
                jpa.getQuantity(), jpa.getCbm(), jpa.getVolumeWeightKg());
        domain.assignIdentity(jpa.getHouseBlAirDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public HouseBlDim toTruckDimDomain(HouseBlTruckDimJpaEntity jpa) {
        HouseBlDim domain = HouseBlDim.create(
                jpa.getHouseBlTruckId(),
                jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm(),
                jpa.getQuantity(), jpa.getCbm(), jpa.getVolumeWeightKg());
        domain.assignIdentity(jpa.getHouseBlTruckDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public HouseBlDim toNonBlDimDomain(HouseBlNonBlDimJpaEntity jpa) {
        HouseBlDim domain = HouseBlDim.create(
                jpa.getHouseBlNonBlId(),
                jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm(),
                jpa.getQuantity(), jpa.getCbm(), jpa.getVolumeWeightKg());
        domain.assignIdentity(jpa.getHouseBlNonBlDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public HouseBlAirDimJpaEntity toAirDimJpa(HouseBlDim d) {
        HouseBlAirDimJpaEntity jpa = new HouseBlAirDimJpaEntity();
        if (d.getId() != null) jpa.setHouseBlAirDimId(d.getId());
        jpa.setLengthCm(d.getLengthCm());
        jpa.setWidthCm(d.getWidthCm());
        jpa.setHeightCm(d.getHeightCm());
        jpa.setQuantity(d.getQuantity());
        jpa.setCbm(d.getCbm());
        jpa.setVolumeWeightKg(d.getVolumeWeightKg());
        return jpa;
    }

    public HouseBlTruckDimJpaEntity toTruckDimJpa(HouseBlDim d) {
        HouseBlTruckDimJpaEntity jpa = new HouseBlTruckDimJpaEntity();
        if (d.getId() != null) jpa.setHouseBlTruckDimId(d.getId());
        jpa.setLengthCm(d.getLengthCm());
        jpa.setWidthCm(d.getWidthCm());
        jpa.setHeightCm(d.getHeightCm());
        jpa.setQuantity(d.getQuantity());
        jpa.setCbm(d.getCbm());
        jpa.setVolumeWeightKg(d.getVolumeWeightKg());
        return jpa;
    }

    public HouseBlNonBlDimJpaEntity toNonBlDimJpa(HouseBlDim d) {
        HouseBlNonBlDimJpaEntity jpa = new HouseBlNonBlDimJpaEntity();
        if (d.getId() != null) jpa.setHouseBlNonBlDimId(d.getId());
        jpa.setLengthCm(d.getLengthCm());
        jpa.setWidthCm(d.getWidthCm());
        jpa.setHeightCm(d.getHeightCm());
        jpa.setQuantity(d.getQuantity());
        jpa.setCbm(d.getCbm());
        jpa.setVolumeWeightKg(d.getVolumeWeightKg());
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
        jpa.setPkgQty(mapOrNull(c.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(c.getPkgUnit());
        jpa.setGrossWeightKg(mapOrNull(c.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(c.getCbm(), Volume::cbm));
        jpa.setSeq(c.getSeq());
        // sealNo4-6, netWeightKg, vgmKg, isSoc는 NonBl form 미사용 필드.
        // INSERT 시 DB 기본값(false/1 등)을 유지하고, UPDATE 시 copyContainerFields에서 set 생략.
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
