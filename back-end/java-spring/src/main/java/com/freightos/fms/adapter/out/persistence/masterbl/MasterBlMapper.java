package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.FlightType;
import com.freightos.fms.domain.common.enums.PackageUnit;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.SecurityStatus;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlDim;
import com.freightos.fms.domain.masterbl.entity.MasterBlScheduleLeg;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.freightos.fms.common.util.VoMapper.mapOrNull;

/**
 * JPA ↔ Domain 변환 매퍼 — Master B/L.
 * toDomain: jobDiv 문자열 비교로 분기 (instanceof 패턴 제거).
 * applyCommonFields/applySeaFields/applyAirFields: PersistenceAdapter에서 직접 호출.
 */
@Component
public class MasterBlMapper {

    // ── JpaEntity → Domain ─────────────────────────────────────────

    public MasterBl toDomain(MasterBlJpaEntity jpa) {
        MasterBlJobDiv jobDiv = MasterBlJobDiv.fromCode(jpa.getJobDiv());
        if (jobDiv == null) throw new IllegalArgumentException("Unknown jobDiv: " + jpa.getJobDiv());
        return switch (jobDiv) {
            case SEA -> toSeaDomain(jpa, jpa.getSeaExt());
            case AIR -> toAirDomain(jpa, jpa.getAirExt());
        };
    }

    private MasterBlSea toSeaDomain(MasterBlJpaEntity jpa, MasterBlSeaJpaEntity seaJpa) {
        MasterBlSea domain = MasterBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (seaJpa != null) copySeaFields(seaJpa, domain);
        return domain;
    }

    private MasterBlAir toAirDomain(MasterBlJpaEntity jpa, MasterBlAirJpaEntity airJpa) {
        MasterBlAir domain = MasterBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (airJpa != null) copyAirFields(airJpa, domain);
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
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()), PackageUnit.fromCode(jpa.getPkgUnit()),
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm())));
    }

    private void copySeaFields(MasterBlSeaJpaEntity jpa, MasterBlSea domain) {
        domain.updateSeaFields(jpa.getLoadType(), LinerCode.of(jpa.getLinerCode()),
                VesselVoyage.of(jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getOnboardDate()), BlNumber.of(jpa.getLineBkgNo()),
                BlDate.of(jpa.getIssueDate()));
    }

    private void copyAirFields(MasterBlAirJpaEntity jpa, MasterBlAir domain) {
        domain.updateAirFields(new MasterBlAir.AirFields(
                AirlineCode.of(jpa.getAirlineCode()),
                BlNumber.of(jpa.getMawbNo()),
                Weight.of(jpa.getChargeWeightKg()), Weight.of(jpa.getVolumeWeightKg()),
                RateClass.fromCode(jpa.getRateClass()), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(),
                SecurityStatus.fromCode(jpa.getSecurityStatus()), FlightType.fromCode(jpa.getFlightType()),
                BlDate.of(jpa.getIssueDate()), PortCode.of(jpa.getIssuePlace()), jpa.getSignature()));
    }

    // ── Domain → JpaEntity (PersistenceAdapter에서 호출) ──────────

    public void applyCommonFields(MasterBl domain, MasterBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setMasterBlId(domain.getId());
        jpa.setMblNo(mapOrNull(domain.getMblNo(), BlNumber::value));
        jpa.setMasterRefNo(mapOrNull(domain.getMasterRefNo(), BlNumber::value));
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(mapOrNull(domain.getJobDiv(), MasterBlJobDiv::name));
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
        jpa.setPkgUnit(mapOrNull(domain.getPkgUnit(), PackageUnit::name));
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
    }

    public void applySeaFields(MasterBlSea domain, MasterBlSeaJpaEntity jpa) {
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(mapOrNull(domain.getLinerCode(), LinerCode::value));
        if (domain.getVesselVoyage() != null) {
            jpa.setVesselName(domain.getVesselVoyage().vesselName());
            jpa.setVoyageNo(domain.getVesselVoyage().voyageNo());
        }
        jpa.setOnboardDate(mapOrNull(domain.getOnboardDate(), BlDate::asString));
        jpa.setLineBkgNo(mapOrNull(domain.getLineBkgNo(), BlNumber::value));
        jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
    }

    public void applyAirFields(MasterBlAir domain, MasterBlAirJpaEntity jpa) {
        jpa.setAirlineCode(mapOrNull(domain.getAirlineCode(), AirlineCode::value));
        jpa.setMawbNo(mapOrNull(domain.getMawbNo(), BlNumber::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setVolumeWeightKg(mapOrNull(domain.getVolumeWeightKg(), Weight::kg));
        jpa.setRateClass(mapOrNull(domain.getRateClass(), RateClass::name));
        jpa.setCurrencyCode(mapOrNull(domain.getCurrencyCode(), CurrencyCode::value));
        jpa.setDeclaredValueCarriage(domain.getDeclaredValueCarriage());
        jpa.setDeclaredValueCustoms(domain.getDeclaredValueCustoms());
        jpa.setInsurance(domain.getInsurance());
        jpa.setAccountInformation(domain.getAccountInformation());
        jpa.setSecurityStatus(mapOrNull(domain.getSecurityStatus(), SecurityStatus::name));
        jpa.setFlightType(mapOrNull(domain.getFlightType(), FlightType::name));
        jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
        jpa.setIssuePlace(mapOrNull(domain.getIssuePlace(), PortCode::value));
        jpa.setSignature(domain.getSignature());
    }

    // ── E-05 DIM ──────────────────────────────────────────────────────

    public MasterBlDim toDimDomain(MasterBlDimJpaEntity jpa) {
        MasterBlDim domain = MasterBlDim.create(
                jpa.getMasterBl().getMasterBlId(),
                jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm(),
                jpa.getQuantity(), jpa.getCbm(), jpa.getVolumeWeightKg(),
                jpa.getSeq());
        domain.assignIdentity(jpa.getMasterBlDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public List<MasterBlDim> toDimDomainList(List<MasterBlDimJpaEntity> jpaList) {
        return jpaList.stream().map(this::toDimDomain).collect(Collectors.toList());
    }

    public void applyDimFields(MasterBlDim domain, MasterBlDimJpaEntity jpa, MasterBlJpaEntity masterBlJpa) {
        jpa.setMasterBl(masterBlJpa);
        jpa.setLengthCm(domain.getLengthCm());
        jpa.setWidthCm(domain.getWidthCm());
        jpa.setHeightCm(domain.getHeightCm());
        jpa.setQuantity(domain.getQuantity());
        jpa.setCbm(domain.getCbm());
        jpa.setVolumeWeightKg(domain.getVolumeWeightKg());
        jpa.setSeq(domain.getSeq());
    }

    // ── E-06 DESC ─────────────────────────────────────────────────────

    public MasterBlDesc toDescDomain(MasterBlDescJpaEntity jpa) {
        MasterBlDesc domain = MasterBlDesc.create(jpa.getMasterBl().getMasterBlId());
        domain.assignIdentity(jpa.getMasterBlDescId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateContent(jpa.getMarks(), jpa.getDescription(),
                jpa.getDescClause1(), jpa.getDescClause2(), jpa.getRemark());
        return domain;
    }

    public Optional<MasterBlDesc> toDescDomain(Optional<MasterBlDescJpaEntity> jpa) {
        return jpa.map(this::toDescDomain);
    }

    public void applyDescFields(MasterBlDesc domain, MasterBlDescJpaEntity jpa, MasterBlJpaEntity masterBlJpa) {
        jpa.setMasterBl(masterBlJpa);
        jpa.setMarks(domain.getMarks());
        jpa.setDescription(domain.getDescription());
        jpa.setDescClause1(domain.getDescClause1());
        jpa.setDescClause2(domain.getDescClause2());
        jpa.setRemark(domain.getRemark());
    }

    // ── E-07 SCHEDULE LEG ─────────────────────────────────────────────

    public MasterBlScheduleLeg toScheduleLegDomain(MasterBlScheduleLegJpaEntity jpa) {
        MasterBlScheduleLeg domain = MasterBlScheduleLeg.create(
                jpa.getMasterBl().getMasterBlId(),
                jpa.getToCode(), jpa.getOnBoardDt(), jpa.getArrivalDt(), jpa.getSeq());
        domain.assignIdentity(jpa.getMasterBlScheduleLegId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateDetails(jpa.getToCode(), jpa.getByCarrier(), jpa.getFlightNo(),
                jpa.getOnBoardDt(), jpa.getOnBoardTm(),
                jpa.getArrivalDt(), jpa.getArrivalTm(), jpa.getSeq());
        return domain;
    }

    public List<MasterBlScheduleLeg> toScheduleLegDomainList(List<MasterBlScheduleLegJpaEntity> jpaList) {
        return jpaList.stream().map(this::toScheduleLegDomain).collect(Collectors.toList());
    }

    public void applyScheduleLegFields(MasterBlScheduleLeg domain, MasterBlScheduleLegJpaEntity jpa,
                                       MasterBlJpaEntity masterBlJpa) {
        jpa.setMasterBl(masterBlJpa);
        jpa.setToCode(domain.getToCode());
        jpa.setByCarrier(domain.getByCarrier());
        jpa.setFlightNo(domain.getFlightNo());
        jpa.setOnBoardDt(domain.getOnBoardDt());
        jpa.setOnBoardTm(domain.getOnBoardTm());
        jpa.setArrivalDt(domain.getArrivalDt());
        jpa.setArrivalTm(domain.getArrivalTm());
        jpa.setSeq(domain.getSeq());
    }
}
