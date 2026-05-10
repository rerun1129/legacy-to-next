package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlAirCharge;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlDim;
import com.freightos.fms.domain.masterbl.entity.MasterBlScheduleLeg;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.freightos.common.util.VoMapper.mapOrNull;

/**
 * JPA ↔ Domain 변환 매퍼 — Master B/L.
 * toDomain: jobDiv 문자열 비교로 분기 (instanceof 패턴 제거).
 * applyCommonFields/applySeaFields/applyAirFields: PersistenceAdapter에서 직접 호출.
 */
@Component
public class MasterBlMapper {

    // ── JpaEntity → Domain ─────────────────────────────────────────

    public MasterBlSea toSeaDomain(MasterBlJpaEntity jpa, MasterBlSeaJpaEntity seaJpa,
                                    MasterBlSeaDescJpaEntity seaDescJpa) {
        MasterBlSea domain = MasterBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (seaJpa != null) copySeaFields(seaJpa, domain);
        if (seaDescJpa != null) domain.initDesc(toSeaDescDomain(seaDescJpa));
        return domain;
    }

    /** FK가 master_bl_air_id로 이전된 이후 airCharges는 airJpa에서 전달받는다 (Step 1.5). */
    public MasterBlAir toAirDomain(MasterBlJpaEntity jpa, MasterBlAirJpaEntity airJpa,
                                   List<MasterBlAirChargeJpaEntity> airChargeJpaList,
                                   MasterBlAirDescJpaEntity airDescJpa) {
        MasterBlAir domain = MasterBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (airJpa != null) copyAirFields(airJpa, domain);
        List<MasterBlAirCharge> airCharges = airChargeJpaList.stream()
                .map(this::toAirChargeDomain)
                .collect(Collectors.toList());
        domain.initAirCharges(airCharges);
        domain.initDims(toDimDomainList(jpa.getDims()));
        List<MasterBlScheduleLegJpaEntity> legJpaList = airJpa != null ? airJpa.getScheduleLegs() : List.of();
        domain.initScheduleLegs(toScheduleLegDomainList(legJpaList));
        if (airDescJpa != null) domain.initDesc(toAirDescDomain(airDescJpa));
        return domain;
    }

    private void copyBaseFields(MasterBlJpaEntity jpa, MasterBl domain) {
        domain.assignIdentity(jpa.getMasterBlId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.assignMblNo(BlNumber.of(jpa.getMblNo()), BlNumber.of(jpa.getMasterRefNo()));
        domain.assignParties(
                CustomerCode.of(jpa.getShipperCode(), jpa.getShipperAddress()),
                CustomerCode.of(jpa.getConsigneeCode(), jpa.getConsigneeAddress()),
                CustomerCode.of(jpa.getNotifyCode(), jpa.getNotifyAddress()));
        domain.updateSchedule(PortCode.of(jpa.getPolCode()), PortCode.of(jpa.getPodCode()),
                BlDate.of(jpa.getEtd()), BlDate.of(jpa.getEta()));
        domain.updateFreightAndOperator(jpa.getFreightTerm(), EmployeeCode.of(jpa.getOperatorCode()),
                TeamCode.of(jpa.getTeamCode()));
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()), null, // weightUnit: sea 확장 테이블에서 별도 로드
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm())));
        domain.assignSettlePartner(CustomerCode.of(jpa.getSettlePartnerCode()));
        domain.updateTradeInfo(jpa.getMainItemName(), jpa.getHsCode());
        domain.updateShipmentType(jpa.getShipmentType());
    }

    private void copySeaFields(MasterBlSeaJpaEntity jpa, MasterBlSea domain) {
        domain.updateSeaFields(jpa.getLoadType(), LinerCode.of(jpa.getLinerCode()),
                VesselVoyage.of(jpa.getVesselCode(), jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getOnboardDate()), BlNumber.of(jpa.getLineBkgNo()),
                BlDate.of(jpa.getIssueDate()));
        domain.updateVesselNationality(jpa.getVesselNationality());
        domain.updateWeightUnit(jpa.getWeightUnit());
        domain.updateServiceTerm(jpa.getServiceTerm());
        domain.updateBlType(jpa.getBlType());
        domain.updateRoute(PortCode.of(jpa.getPorCode()), PortCode.of(jpa.getFinalDestCode()));
        domain.updateRton(Rton.of(jpa.getRton()));
    }

    private void copyAirFields(MasterBlAirJpaEntity jpa, MasterBlAir domain) {
        domain.updateAirFields(new MasterBlAir.AirFields(
                AirlineCode.of(jpa.getAirlineCode()),
                Weight.of(jpa.getChargeWeightKg()), Weight.of(jpa.getVolumeWeightKg()),
                jpa.getRateClass(), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(),
                jpa.getSecurityStatus(), jpa.getFlightType(),
                BlDate.of(jpa.getIssueDate()), PortCode.of(jpa.getIssuePlace()), jpa.getSignature(),
                jpa.getOtherTerm(),
                HandlingInformation.of(jpa.getHandlingInfoCode(), jpa.getHandlingInfoText())));
    }

    // ── Domain → JpaEntity (PersistenceAdapter에서 호출) ──────────

    public void applyCommonFields(MasterBl domain, MasterBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setMasterBlId(domain.getId());
        jpa.setMblNo(mapOrNull(domain.getMblNo(), BlNumber::value));
        jpa.setMasterRefNo(mapOrNull(domain.getMasterRefNo(), BlNumber::value));
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(domain.getJobDiv());
        jpa.setShipperCode(mapOrNull(domain.getShipperCode(), CustomerCode::value));
        jpa.setShipperAddress(mapOrNull(domain.getShipperCode(), CustomerCode::address));
        jpa.setConsigneeCode(mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
        jpa.setConsigneeAddress(mapOrNull(domain.getConsigneeCode(), CustomerCode::address));
        jpa.setNotifyCode(mapOrNull(domain.getNotifyCode(), CustomerCode::value));
        jpa.setNotifyAddress(mapOrNull(domain.getNotifyCode(), CustomerCode::address));
        jpa.setPolCode(mapOrNull(domain.getPolCode(), PortCode::value));
        jpa.setPodCode(mapOrNull(domain.getPodCode(), PortCode::value));
        jpa.setEtd(mapOrNull(domain.getEtd(), BlDate::asString));
        jpa.setEta(mapOrNull(domain.getEta(), BlDate::asString));
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setOperatorCode(mapOrNull(domain.getOperatorCode(), EmployeeCode::value));
        jpa.setTeamCode(mapOrNull(domain.getTeamCode(), TeamCode::value));
        jpa.setPkgQty(mapOrNull(domain.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(mapOrNull(domain.getPkgUnit(), WeightUnit::name));
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setMainItemName(domain.getMainItemName());
        jpa.setHsCode(domain.getHsCode());
        jpa.setShipmentType(domain.getShipmentType());
    }

    public void applySeaFields(MasterBlSea domain, MasterBlSeaJpaEntity jpa) {
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(mapOrNull(domain.getLinerCode(), LinerCode::value));
        if (domain.getVesselVoyage() != null) {
            jpa.setVesselName(domain.getVesselVoyage().vesselName());
            jpa.setVoyageNo(domain.getVesselVoyage().voyageNo());
            jpa.setVesselCode(domain.getVesselVoyage().vesselCode());
        }
        jpa.setOnboardDate(mapOrNull(domain.getOnboardDate(), BlDate::asString));
        jpa.setLineBkgNo(mapOrNull(domain.getLineBkgNo(), BlNumber::value));
        jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
        jpa.setVesselNationality(domain.getVesselNationality());
        jpa.setWeightUnit(domain.getWeightUnit());
        jpa.setServiceTerm(domain.getServiceTerm());
        jpa.setBlType(domain.getBlType());
        jpa.setPorCode(mapOrNull(domain.getPorCode(), PortCode::value));
        jpa.setFinalDestCode(mapOrNull(domain.getFinalDestCode(), PortCode::value));
        jpa.setRton(mapOrNull(domain.getRton(), Rton::ton));
    }

    public void applyAirFields(MasterBlAir domain, MasterBlAirJpaEntity jpa) {
        jpa.setAirlineCode(mapOrNull(domain.getAirlineCode(), AirlineCode::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setVolumeWeightKg(mapOrNull(domain.getVolumeWeightKg(), Weight::kg));
        jpa.setRateClass(domain.getRateClass());
        jpa.setCurrencyCode(mapOrNull(domain.getCurrencyCode(), CurrencyCode::value));
        jpa.setDeclaredValueCarriage(domain.getDeclaredValueCarriage());
        jpa.setDeclaredValueCustoms(domain.getDeclaredValueCustoms());
        jpa.setInsurance(domain.getInsurance());
        jpa.setAccountInformation(domain.getAccountInformation());
        jpa.setSecurityStatus(domain.getSecurityStatus());
        jpa.setFlightType(domain.getFlightType());
        jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
        jpa.setIssuePlace(mapOrNull(domain.getIssuePlace(), PortCode::value));
        jpa.setSignature(domain.getSignature());
        jpa.setOtherTerm(domain.getOtherTerm());
        jpa.setHandlingInfoCode(mapOrNull(domain.getHandlingInformation(), HandlingInformation::code));
        jpa.setHandlingInfoText(mapOrNull(domain.getHandlingInformation(), HandlingInformation::description));
    }

    // ── E-05 DIM ──────────────────────────────────────────────────────

    public MasterBlDim toDimDomain(MasterBlDimJpaEntity jpa) {
        MasterBlDim domain = MasterBlDim.create(
                jpa.getMasterBlId(),
                jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm(),
                jpa.getQuantity(), jpa.getCbm(), jpa.getVolumeWeightKg());
        domain.assignIdentity(jpa.getMasterBlDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public List<MasterBlDim> toDimDomainList(List<MasterBlDimJpaEntity> jpaList) {
        return jpaList.stream().map(this::toDimDomain).collect(Collectors.toList());
    }

    public void applyDimFields(MasterBlDim domain, MasterBlDimJpaEntity jpa, MasterBlJpaEntity masterBlJpa) {
        jpa.setLengthCm(domain.getLengthCm());
        jpa.setWidthCm(domain.getWidthCm());
        jpa.setHeightCm(domain.getHeightCm());
        jpa.setQuantity(domain.getQuantity());
        jpa.setCbm(domain.getCbm());
        jpa.setVolumeWeightKg(domain.getVolumeWeightKg());
    }

    // ── E-06 DESC ─────────────────────────────────────────────────────

    public MasterBlDesc toSeaDescDomain(MasterBlSeaDescJpaEntity jpa) {
        MasterBlDesc domain = MasterBlDesc.create(jpa.getSea().getMasterBlSeaId());
        domain.assignIdentity(jpa.getMasterBlSeaDescId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateContent(jpa.getMarks(), jpa.getDescription(),
                jpa.getDescClause1(), jpa.getDescClause2(), jpa.getRemark());
        return domain;
    }

    public MasterBlDesc toAirDescDomain(MasterBlAirDescJpaEntity jpa) {
        MasterBlDesc domain = MasterBlDesc.create(jpa.getAir().getMasterBlAirId());
        domain.assignIdentity(jpa.getMasterBlAirDescId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateContent(jpa.getMarks(), jpa.getDescription(),
                jpa.getDescClause1(), jpa.getDescClause2(), jpa.getRemark());
        return domain;
    }

    public void applySeaDescFields(MasterBlDesc domain, MasterBlSeaDescJpaEntity jpa, MasterBlSeaJpaEntity seaJpa) {
        jpa.setSea(seaJpa);
        jpa.setMarks(domain.getMarks());
        jpa.setDescription(domain.getDescription());
        jpa.setDescClause1(domain.getDescClause1());
        jpa.setDescClause2(domain.getDescClause2());
        jpa.setRemark(domain.getRemark());
    }

    public void applyAirDescFields(MasterBlDesc domain, MasterBlAirDescJpaEntity jpa, MasterBlAirJpaEntity airJpa) {
        jpa.setAir(airJpa);
        jpa.setMarks(domain.getMarks());
        jpa.setDescription(domain.getDescription());
        jpa.setDescClause1(domain.getDescClause1());
        jpa.setDescClause2(domain.getDescClause2());
        jpa.setRemark(domain.getRemark());
    }

    // ── E-07 SCHEDULE LEG ─────────────────────────────────────────────

    public MasterBlScheduleLeg toScheduleLegDomain(MasterBlScheduleLegJpaEntity jpa) {
        MasterBlScheduleLeg domain = MasterBlScheduleLeg.create(
                jpa.getMasterBlAirId(),
                jpa.getToCode(), jpa.getOnBoardDt(), jpa.getArrivalDt());
        domain.assignIdentity(jpa.getMasterBlScheduleLegId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateDetails(jpa.getToCode(), jpa.getByCarrier(), jpa.getFlightNo(),
                jpa.getOnBoardDt(), jpa.getOnBoardTm(),
                jpa.getArrivalDt(), jpa.getArrivalTm());
        return domain;
    }

    public List<MasterBlScheduleLeg> toScheduleLegDomainList(List<MasterBlScheduleLegJpaEntity> jpaList) {
        return jpaList.stream().map(this::toScheduleLegDomain).collect(Collectors.toList());
    }

    /** FK(master_bl_air_id)는 MasterBlAirJpaEntity.syncScheduleLegs(@JoinColumn)이 설정 — airJpa 인자 불필요 */
    public void applyScheduleLegFields(MasterBlScheduleLeg domain, MasterBlScheduleLegJpaEntity jpa) {
        jpa.setToCode(domain.getToCode());
        jpa.setByCarrier(domain.getByCarrier());
        jpa.setFlightNo(domain.getFlightNo());
        jpa.setOnBoardDt(domain.getOnBoardDt());
        jpa.setOnBoardTm(domain.getOnBoardTm());
        jpa.setArrivalDt(domain.getArrivalDt());
        jpa.setArrivalTm(domain.getArrivalTm());
    }

    public MasterBlDimJpaEntity toDimJpa(MasterBlDim domain, MasterBlJpaEntity masterBl) {
        MasterBlDimJpaEntity jpa = new MasterBlDimJpaEntity();
        applyDimFields(domain, jpa, masterBl);
        return jpa;
    }

    /** FK(master_bl_air_id)는 MasterBlAirJpaEntity.syncScheduleLegs(@JoinColumn)이 설정 — airJpa 인자 불필요 */
    public MasterBlScheduleLegJpaEntity toScheduleLegJpa(MasterBlScheduleLeg domain) {
        MasterBlScheduleLegJpaEntity jpa = new MasterBlScheduleLegJpaEntity();
        applyScheduleLegFields(domain, jpa);
        return jpa;
    }

    // ── AIR CHARGE ───────────────────────────────────────────────────

    public MasterBlAirCharge toAirChargeDomain(MasterBlAirChargeJpaEntity jpa) {
        MasterBlAirCharge c = MasterBlAirCharge.create(jpa.getMasterBlAirId());
        c.assignIdentity(jpa.getMasterBlAirChargeId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(new MasterBlAirCharge.Details(
                jpa.getFreightCode(), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getPer(), jpa.getFreightTerm(),
                Weight.of(jpa.getGrossWeightKg()), jpa.getRateClass(),
                Weight.of(jpa.getChargeWeightKg()), jpa.getRate()));
        return c;
    }

    public List<MasterBlAirCharge> toAirChargeDomainList(List<MasterBlAirChargeJpaEntity> jpaList) {
        return jpaList.stream().map(this::toAirChargeDomain).collect(Collectors.toList());
    }

    /** FK(master_bl_air_id)는 MasterBlAirJpaEntity.syncAirCharges(@JoinColumn)이 설정 — parent 인자 불필요 */
    public void applyAirChargeFields(MasterBlAirCharge domain, MasterBlAirChargeJpaEntity jpa) {
        jpa.setFreightCode(domain.getFreightCode());
        jpa.setCurrencyCode(mapOrNull(domain.getCurrencyCode(), CurrencyCode::value));
        jpa.setPer(domain.getPer());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setRateClass(domain.getRateClass());
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setRate(domain.getRate());
    }

    /** FK(master_bl_air_id)는 MasterBlAirJpaEntity.syncAirCharges(@JoinColumn)이 설정 — parent 인자 불필요 */
    public MasterBlAirChargeJpaEntity toAirChargeJpa(MasterBlAirCharge c) {
        MasterBlAirChargeJpaEntity jpa = new MasterBlAirChargeJpaEntity();
        applyAirChargeFields(c, jpa);
        return jpa;
    }

    public MasterBlSeaDescJpaEntity toSeaDescJpa(MasterBlDesc d, MasterBlSeaJpaEntity seaJpa) {
        MasterBlSeaDescJpaEntity jpa = new MasterBlSeaDescJpaEntity();
        applySeaDescFields(d, jpa, seaJpa);
        return jpa;
    }

    public MasterBlAirDescJpaEntity toAirDescJpa(MasterBlDesc d, MasterBlAirJpaEntity airJpa) {
        MasterBlAirDescJpaEntity jpa = new MasterBlAirDescJpaEntity();
        applyAirDescFields(d, jpa, airJpa);
        return jpa;
    }
}
