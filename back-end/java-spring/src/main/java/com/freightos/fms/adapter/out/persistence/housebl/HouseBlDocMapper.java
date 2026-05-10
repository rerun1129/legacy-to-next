package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.freightos.common.util.VoMapper.mapOrNull;

/**
 * House B/L 문서(DESC·ScheduleLeg·TruckOrder·AirCharge) JPA ↔ Domain 변환 매퍼.
 */
@Component
public class HouseBlDocMapper {

    // ── E-13 DESC ─────────────────────────────────────────────────────

    public HouseBlDesc toDescDomain(HouseBlDescJpaEntity jpa) {
        HouseBlDesc domain = HouseBlDesc.create(jpa.getHouseBl().getHouseBlId());
        domain.assignIdentity(jpa.getHouseBlDescId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateContent(jpa.getMarks(), jpa.getDescription(),
                jpa.getDescClause1(), jpa.getDescClause2(), jpa.getRemark());
        return domain;
    }

    public Optional<HouseBlDesc> toDescDomain(Optional<HouseBlDescJpaEntity> jpa) {
        return jpa.map(this::toDescDomain);
    }

    public void applyDescFields(HouseBlDesc domain, HouseBlDescJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setHouseBl(houseBlJpa);
        jpa.setMarks(domain.getMarks());
        jpa.setDescription(domain.getDescription());
        jpa.setDescClause1(domain.getDescClause1());
        jpa.setDescClause2(domain.getDescClause2());
        jpa.setRemark(domain.getRemark());
    }

    public HouseBlDescJpaEntity toDescJpa(HouseBlDesc d, HouseBlJpaEntity houseBl) {
        HouseBlDescJpaEntity jpa = new HouseBlDescJpaEntity();
        if (d.getId() != null) jpa.setHouseBlDescId(d.getId());
        applyDescFields(d, jpa, houseBl);
        return jpa;
    }

    // ── E-19 SCHEDULE LEG ─────────────────────────────────────────────

    public HouseBlScheduleLeg toScheduleLegDomain(HouseBlScheduleLegJpaEntity jpa) {
        HouseBlScheduleLeg domain = HouseBlScheduleLeg.create(
                jpa.getHouseBlAirId(),
                jpa.getToCode(), jpa.getOnBoardDt(), jpa.getArrivalDt());
        domain.assignIdentity(jpa.getHouseBlScheduleLegId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateDetails(jpa.getToCode(), jpa.getByCarrier(), jpa.getFlightNo(),
                jpa.getOnBoardDt(), jpa.getOnBoardTm(), jpa.getArrivalDt(), jpa.getArrivalTm());
        return domain;
    }

    public List<HouseBlScheduleLeg> toScheduleLegDomainList(List<HouseBlScheduleLegJpaEntity> jpaList) {
        return jpaList.stream().map(this::toScheduleLegDomain).collect(Collectors.toList());
    }

    public void applyScheduleLegFields(HouseBlScheduleLeg domain, HouseBlScheduleLegJpaEntity jpa) {
        jpa.setToCode(domain.getToCode());
        jpa.setByCarrier(domain.getByCarrier());
        jpa.setFlightNo(domain.getFlightNo());
        jpa.setOnBoardDt(domain.getOnBoardDt());
        jpa.setOnBoardTm(domain.getOnBoardTm());
        jpa.setArrivalDt(domain.getArrivalDt());
        jpa.setArrivalTm(domain.getArrivalTm());
    }

    /** FK(house_bl_air_id)는 HouseBlAirJpaEntity.syncScheduleLegs(@JoinColumn)이 설정 — airJpa 인자 불필요 */
    public HouseBlScheduleLegJpaEntity toScheduleLegJpa(HouseBlScheduleLeg leg) {
        HouseBlScheduleLegJpaEntity jpa = new HouseBlScheduleLegJpaEntity();
        applyScheduleLegFields(leg, jpa);
        return jpa;
    }

    // ── E-20 TRUCK ORDER ──────────────────────────────────────────────

    public HouseBlTruckOrder toTruckOrderDomain(HouseBlTruckOrderJpaEntity jpa) {
        HouseBlTruckOrder o = HouseBlTruckOrder.create(jpa.getHouseBlTruckId());
        o.assignIdentity(jpa.getHouseBlTruckOrderId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        o.updateDetails(new HouseBlTruckOrder.Details(
                jpa.getTruckOrderNo(), jpa.getPkgQty(), jpa.getPkgUnit(),
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm()),
                jpa.getTruckNo(), jpa.getTruckType(),
                jpa.getDriver(), jpa.getMobileNo(),
                ContainerNumber.of(jpa.getContainerNo()), jpa.getContainerType(),
                SealNumber.of(jpa.getSealNo1()), SealNumber.of(jpa.getSealNo2()), SealNumber.of(jpa.getSealNo3())));
        return o;
    }

    public List<HouseBlTruckOrder> toTruckOrderDomainList(List<HouseBlTruckOrderJpaEntity> jpaList) {
        return jpaList.stream().map(this::toTruckOrderDomain).collect(Collectors.toList());
    }

    /** FK(house_bl_truck_id)는 HouseBlTruckJpaEntity.syncTruckOrders(@JoinColumn)이 설정 — truckJpa 인자 불필요 */
    public void applyTruckOrderFields(HouseBlTruckOrder domain, HouseBlTruckOrderJpaEntity jpa) {
        jpa.setTruckOrderNo(domain.getTruckOrderNo());
        jpa.setPkgQty(domain.getPkgQty());
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setTruckNo(domain.getTruckNo());
        jpa.setTruckType(domain.getTruckType());
        jpa.setDriver(domain.getDriver());
        jpa.setMobileNo(domain.getMobileNo());
        jpa.setContainerNo(mapOrNull(domain.getContainerNo(), ContainerNumber::value));
        jpa.setContainerType(domain.getContainerType());
        jpa.setSealNo1(mapOrNull(domain.getSealNo1(), SealNumber::value));
        jpa.setSealNo2(mapOrNull(domain.getSealNo2(), SealNumber::value));
        jpa.setSealNo3(mapOrNull(domain.getSealNo3(), SealNumber::value));
    }

    /** FK(house_bl_truck_id)는 HouseBlTruckJpaEntity.syncTruckOrders(@JoinColumn)이 설정 — truckJpa 인자 불필요 */
    public HouseBlTruckOrderJpaEntity toTruckOrderJpa(HouseBlTruckOrder o) {
        HouseBlTruckOrderJpaEntity jpa = new HouseBlTruckOrderJpaEntity();
        applyTruckOrderFields(o, jpa);
        return jpa;
    }

    // ── E-21 AIR CHARGE ──────────────────────────────────────────────

    public HouseBlAirCharge toAirChargeDomain(HouseBlAirChargeJpaEntity jpa) {
        HouseBlAirCharge c = HouseBlAirCharge.create(jpa.getHouseBlAirId());
        c.assignIdentity(jpa.getHouseBlAirChargeId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(new HouseBlAirCharge.Details(
                jpa.getFreightCode(), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getPer(), jpa.getFreightTerm(),
                Weight.of(jpa.getGrossWeightKg()), jpa.getRateClass(),
                Weight.of(jpa.getChargeWeightKg()), jpa.getRate()));
        return c;
    }

    public List<HouseBlAirCharge> toAirChargeDomainList(List<HouseBlAirChargeJpaEntity> jpaList) {
        return jpaList.stream().map(this::toAirChargeDomain).collect(Collectors.toList());
    }

    /** FK(house_bl_air_id)는 HouseBlAirJpaEntity.syncAirCharges(@JoinColumn)이 설정 — airJpa 인자 불필요 */
    public void applyAirChargeFields(HouseBlAirCharge domain, HouseBlAirChargeJpaEntity jpa) {
        jpa.setFreightCode(domain.getFreightCode());
        jpa.setCurrencyCode(mapOrNull(domain.getCurrencyCode(), CurrencyCode::value));
        jpa.setPer(domain.getPer());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setRateClass(domain.getRateClass());
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setRate(domain.getRate());
    }

    /** FK(house_bl_air_id)는 HouseBlAirJpaEntity.syncAirCharges(@JoinColumn)이 설정 — airJpa 인자 불필요 */
    public HouseBlAirChargeJpaEntity toAirChargeJpa(HouseBlAirCharge c) {
        HouseBlAirChargeJpaEntity jpa = new HouseBlAirChargeJpaEntity();
        applyAirChargeFields(c, jpa);
        return jpa;
    }
}
