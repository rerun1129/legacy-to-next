package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.PackageUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.freightos.fms.common.util.VoMapper.mapOrNull;

/**
 * House B/L 화물(컨테이너·DIM·라이선스) JPA ↔ Domain 변환 매퍼.
 */
@Component
public class HouseBlCargoMapper {

    HouseBlContainerJpaEntity toContainerJpa(HouseBlContainer c, HouseBlJpaEntity jpaParent) {
        HouseBlContainerJpaEntity jpa = HouseBlContainerJpaEntity.of(jpaParent,
                mapOrNull(c.getContainerNo(), ContainerNumber::value),
                mapOrNull(c.getContainerType(), ContainerType::getCode),
                c.getLengthFeet());
        if (c.getId() != null) jpa.setHouseBlContainerId(c.getId());
        jpa.setSealNo1(mapOrNull(c.getSealNo1(), SealNumber::value));
        jpa.setSealNo2(mapOrNull(c.getSealNo2(), SealNumber::value));
        jpa.setPkgQty(mapOrNull(c.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(mapOrNull(c.getPkgUnit(), PackageUnit::name));
        jpa.setGrossWeightKg(mapOrNull(c.getGrossWeightKg(), Weight::kg));
        jpa.setNetWeightKg(mapOrNull(c.getNetWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(c.getCbm(), Volume::cbm));
        jpa.setVgmKg(mapOrNull(c.getVgmKg(), Weight::kg));
        jpa.setIsSoc(c.isSoc());
        jpa.setSeq(c.getSeq());
        return jpa;
    }

    // ── E-12 DIM ──────────────────────────────────────────────────────

    public HouseBlDim toDimDomain(HouseBlDimJpaEntity jpa) {
        HouseBlDim domain = HouseBlDim.create(
                jpa.getHouseBl().getHouseBlId(),
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
        jpa.setHouseBl(houseBlJpa);
        jpa.setLengthCm(domain.getLengthCm());
        jpa.setWidthCm(domain.getWidthCm());
        jpa.setHeightCm(domain.getHeightCm());
        jpa.setQuantity(domain.getQuantity());
        jpa.setCbm(domain.getCbm());
        jpa.setVolumeWeightKg(domain.getVolumeWeightKg());
    }

    public HouseBlDimJpaEntity toDimJpa(HouseBlDim d, HouseBlJpaEntity houseBl) {
        HouseBlDimJpaEntity jpa = new HouseBlDimJpaEntity();
        applyDimFields(d, jpa, houseBl);
        return jpa;
    }

    // ── E-17 LICENSE ──────────────────────────────────────────────────

    public HouseBlLicense toLicenseDomain(HouseBlLicenseJpaEntity jpa) {
        HouseBlLicense domain = HouseBlLicense.create(
                jpa.getHouseBl().getHouseBlId(), jpa.getSeq());
        domain.assignIdentity(jpa.getHouseBlLicenseId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateDetails(jpa.getLicenseNo(), jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getCombinedPackingMark(),
                jpa.getCombinedPackingQty(), jpa.getCombinedPackingUnit(),
                jpa.isPartialShipment(), jpa.getPartialShipmentSeq(), jpa.getHsnNo());
        return domain;
    }

    public List<HouseBlLicense> toLicenseDomainList(List<HouseBlLicenseJpaEntity> jpaList) {
        return jpaList.stream().map(this::toLicenseDomain).collect(Collectors.toList());
    }

    public void applyLicenseFields(HouseBlLicense domain, HouseBlLicenseJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setHouseBl(houseBlJpa);
        jpa.setLicenseNo(domain.getLicenseNo());
        jpa.setPkgQty(domain.getPkgQty());
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setGrossWeightKg(domain.getGrossWeightKg());
        jpa.setCombinedPackingMark(domain.getCombinedPackingMark());
        jpa.setCombinedPackingQty(domain.getCombinedPackingQty());
        jpa.setCombinedPackingUnit(domain.getCombinedPackingUnit());
        jpa.setPartialShipment(domain.isPartialShipment());
        jpa.setPartialShipmentSeq(domain.getPartialShipmentSeq());
        jpa.setHsnNo(domain.getHsnNo());
        jpa.setSeq(domain.getSeq());
    }

    public HouseBlLicenseJpaEntity toLicenseJpa(HouseBlLicense l, HouseBlJpaEntity houseBl) {
        HouseBlLicenseJpaEntity jpa = new HouseBlLicenseJpaEntity();
        applyLicenseFields(l, jpa, houseBl);
        return jpa;
    }
}
