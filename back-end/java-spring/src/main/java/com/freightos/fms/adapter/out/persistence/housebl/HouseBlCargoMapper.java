package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.freightos.common.util.VoMapper.mapOrNull;

/**
 * House B/L 화물(컨테이너·DIM) JPA ↔ Domain 변환 매퍼.
 */
@Component
public class HouseBlCargoMapper {

    HouseBlContainerJpaEntity toContainerJpa(HouseBlContainer c, HouseBlJpaEntity jpaParent) {
        HouseBlContainerJpaEntity jpa = HouseBlContainerJpaEntity.of(
                mapOrNull(c.getContainerNo(), ContainerNumber::value),
                c.getContainerType(),
                c.getLengthFeet());
        if (c.getId() != null) jpa.setHouseBlContainerId(c.getId());
        jpa.setSealNo1(mapOrNull(c.getSealNo1(), SealNumber::value));
        jpa.setSealNo2(mapOrNull(c.getSealNo2(), SealNumber::value));
        jpa.setSealNo3(mapOrNull(c.getSealNo3(), SealNumber::value));
        jpa.setSealNo4(mapOrNull(c.getSealNo4(), SealNumber::value));
        jpa.setSealNo5(mapOrNull(c.getSealNo5(), SealNumber::value));
        jpa.setSealNo6(mapOrNull(c.getSealNo6(), SealNumber::value));
        jpa.setPkgQty(mapOrNull(c.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(mapOrNull(c.getPkgUnit(), WeightUnit::name));
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
        applyDimFields(d, jpa, houseBl);
        return jpa;
    }

}
