package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.springframework.stereotype.Component;

/**
 * JPA ↔ Domain 변환 매퍼 — Master B/L.
 * toDomain: jobDiv 문자열 비교로 분기 (instanceof 패턴 제거).
 * applyCommonFields/applySeaFields/applyAirFields: PersistenceAdapter에서 직접 호출.
 */
@Component
public class MasterBlMapper {

    // ── JpaEntity → Domain ─────────────────────────────────────────

    public MasterBl toDomain(MasterBlJpaEntity jpa) {
        String jobDiv = jpa.getJobDiv();
        return switch (jobDiv) {
            case "SEA" -> toSeaDomain(jpa, jpa.getSeaExt());
            case "AIR" -> toAirDomain(jpa, jpa.getAirExt());
            default    -> throw new IllegalArgumentException("Unknown jobDiv: " + jobDiv);
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
        domain.assignParties(PartyCode.of(jpa.getShipperCode()), PartyCode.of(jpa.getConsigneeCode()),
                PartyCode.of(jpa.getNotifyCode()));
        domain.updateSchedule(PortCode.of(jpa.getPolCode()), PortCode.of(jpa.getPodCode()),
                BlDate.of(jpa.getEtd()), BlDate.of(jpa.getEta()));
        domain.updateFreightAndOperator(jpa.getFreightTerm(), EmployeeCode.of(jpa.getOperatorCode()),
                TeamCode.of(jpa.getTeamCode()));
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()), jpa.getPkgUnit(),
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm())));
    }

    private void copySeaFields(MasterBlSeaJpaEntity jpa, MasterBlSea domain) {
        domain.updateSeaFields(jpa.getLoadType(), PartyCode.of(jpa.getLinerCode()),
                VesselVoyage.of(jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getOnboardDate()), BlNumber.of(jpa.getLineBkgNo()),
                BlDate.of(jpa.getIssueDate()));
    }

    private void copyAirFields(MasterBlAirJpaEntity jpa, MasterBlAir domain) {
        domain.updateAirFields(new MasterBlAir.AirFields(
                PartyCode.of(jpa.getAirlineCode()), AirportCode.of(jpa.getDepartureCode()),
                BlNumber.of(jpa.getMawbNo()),
                Weight.of(jpa.getChargeWeightKg()), Weight.of(jpa.getVolumeWeightKg()),
                jpa.getRateClass(), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(),
                jpa.getSecurityStatus(), jpa.getFlightType(),
                BlDate.of(jpa.getIssueDate()), jpa.getIssuePlace(), jpa.getSignature()));
    }

    // ── Domain → JpaEntity (PersistenceAdapter에서 호출) ──────────

    public void applyCommonFields(MasterBl domain, MasterBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setMasterBlId(domain.getId());
        jpa.setMblNo(domain.getMblNo() != null ? domain.getMblNo().value() : null);
        jpa.setMasterRefNo(domain.getMasterRefNo() != null ? domain.getMasterRefNo().value() : null);
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(switch (domain) {
            case MasterBlSea ignored -> "SEA";
            case MasterBlAir ignored -> "AIR";
            default -> throw new IllegalStateException("Unknown MasterBl subtype: " + domain.getClass().getSimpleName());
        });
        jpa.setShipperCode(domain.getShipperCode() != null ? domain.getShipperCode().value() : null);
        jpa.setConsigneeCode(domain.getConsigneeCode() != null ? domain.getConsigneeCode().value() : null);
        jpa.setNotifyCode(domain.getNotifyCode() != null ? domain.getNotifyCode().value() : null);
        jpa.setPolCode(domain.getPolCode() != null ? domain.getPolCode().value() : null);
        jpa.setPodCode(domain.getPodCode() != null ? domain.getPodCode().value() : null);
        jpa.setEtd(domain.getEtd() != null ? domain.getEtd().asString() : null);
        jpa.setEta(domain.getEta() != null ? domain.getEta().asString() : null);
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setOperatorCode(domain.getOperatorCode() != null ? domain.getOperatorCode().value() : null);
        jpa.setTeamCode(domain.getTeamCode() != null ? domain.getTeamCode().value() : null);
        jpa.setPkgQty(domain.getPkgQty() != null ? domain.getPkgQty().count() : null);
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setGrossWeightKg(domain.getGrossWeightKg() != null ? domain.getGrossWeightKg().kg() : null);
        jpa.setCbm(domain.getCbm() != null ? domain.getCbm().cbm() : null);
    }

    public void applySeaFields(MasterBlSea domain, MasterBlSeaJpaEntity jpa) {
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(domain.getLinerCode() != null ? domain.getLinerCode().value() : null);
        if (domain.getVesselVoyage() != null) {
            jpa.setVesselName(domain.getVesselVoyage().vesselName());
            jpa.setVoyageNo(domain.getVesselVoyage().voyageNo());
        }
        jpa.setOnboardDate(domain.getOnboardDate() != null ? domain.getOnboardDate().asString() : null);
        jpa.setLineBkgNo(domain.getLineBkgNo() != null ? domain.getLineBkgNo().value() : null);
        jpa.setIssueDate(domain.getIssueDate() != null ? domain.getIssueDate().asString() : null);
    }

    public void applyAirFields(MasterBlAir domain, MasterBlAirJpaEntity jpa) {
        jpa.setAirlineCode(domain.getAirlineCode() != null ? domain.getAirlineCode().value() : null);
        jpa.setDepartureCode(domain.getDepartureCode() != null ? domain.getDepartureCode().value() : null);
        jpa.setMawbNo(domain.getMawbNo() != null ? domain.getMawbNo().value() : null);
        jpa.setChargeWeightKg(domain.getChargeWeightKg() != null ? domain.getChargeWeightKg().kg() : null);
        jpa.setVolumeWeightKg(domain.getVolumeWeightKg() != null ? domain.getVolumeWeightKg().kg() : null);
        jpa.setRateClass(domain.getRateClass());
        jpa.setCurrencyCode(domain.getCurrencyCode() != null ? domain.getCurrencyCode().value() : null);
        jpa.setDeclaredValueCarriage(domain.getDeclaredValueCarriage());
        jpa.setDeclaredValueCustoms(domain.getDeclaredValueCustoms());
        jpa.setInsurance(domain.getInsurance());
        jpa.setAccountInformation(domain.getAccountInformation());
        jpa.setSecurityStatus(domain.getSecurityStatus());
        jpa.setFlightType(domain.getFlightType());
        jpa.setIssueDate(domain.getIssueDate() != null ? domain.getIssueDate().asString() : null);
        jpa.setIssuePlace(domain.getIssuePlace());
        jpa.setSignature(domain.getSignature());
    }
}
