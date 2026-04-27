package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
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
        if ("SEA".equals(jobDiv)) {
            return toSeaDomain(jpa, jpa.getSeaExt());
        } else if ("AIR".equals(jobDiv)) {
            return toAirDomain(jpa, jpa.getAirExt());
        }
        throw new IllegalArgumentException("Unknown jobDiv: " + jobDiv);
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
        domain.assignMblNo(jpa.getMblNo(), jpa.getMasterRefNo());
        domain.assignParties(jpa.getShipperCode(), jpa.getConsigneeCode(), jpa.getNotifyCode());
        domain.updateSchedule(jpa.getPolCode(), jpa.getPodCode(), jpa.getEtd(), jpa.getEta());
        domain.updateFreightAndOperator(jpa.getFreightTerm(), jpa.getOperatorCode(), jpa.getTeamCode());
        domain.updateCargoSummary(jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getCbm());
    }

    private void copySeaFields(MasterBlSeaJpaEntity jpa, MasterBlSea domain) {
        domain.updateSeaFields(jpa.getLoadType(), jpa.getLinerCode(), jpa.getVesselName(),
                jpa.getVoyageNo(), jpa.getOnboardDate(),
                jpa.getLineBkgNo(), jpa.getIssueDate());
    }

    private void copyAirFields(MasterBlAirJpaEntity jpa, MasterBlAir domain) {
        domain.updateAirFields(
                jpa.getAirlineCode(), jpa.getDepartureCode(), jpa.getMawbNo(),
                jpa.getChargeWeightKg(), jpa.getVolumeWeightKg(),
                jpa.getRateClass(), jpa.getCurrencyCode(),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(),
                jpa.getSecurityStatus(), jpa.getFlightType(),
                jpa.getIssueDate(), jpa.getIssuePlace(), jpa.getSignature());
    }

    // ── Domain → JpaEntity (PersistenceAdapter에서 호출) ──────────

    public void applyCommonFields(MasterBl domain, MasterBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setMasterBlId(domain.getId());
        jpa.setMblNo(domain.getMblNo());
        jpa.setMasterRefNo(domain.getMasterRefNo());
        jpa.setBound(domain.getBound());
        // jobDiv는 domain 클래스 타입에서 판별
        if (domain instanceof MasterBlSea) jpa.setJobDiv("SEA");
        else if (domain instanceof MasterBlAir) jpa.setJobDiv("AIR");
        jpa.setShipperCode(domain.getShipperCode());
        jpa.setConsigneeCode(domain.getConsigneeCode());
        jpa.setNotifyCode(domain.getNotifyCode());
        jpa.setPolCode(domain.getPolCode());
        jpa.setPodCode(domain.getPodCode());
        jpa.setEtd(domain.getEtd());
        jpa.setEta(domain.getEta());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setOperatorCode(domain.getOperatorCode());
        jpa.setTeamCode(domain.getTeamCode());
        jpa.setPkgQty(domain.getPkgQty());
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setGrossWeightKg(domain.getGrossWeightKg());
        jpa.setCbm(domain.getCbm());
    }

    public void applySeaFields(MasterBlSea domain, MasterBlSeaJpaEntity jpa) {
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(domain.getLinerCode());
        jpa.setVesselName(domain.getVesselName());
        jpa.setVoyageNo(domain.getVoyageNo());
        jpa.setOnboardDate(domain.getOnboardDate());
        jpa.setLineBkgNo(domain.getLineBkgNo());
        jpa.setIssueDate(domain.getIssueDate());
    }

    public void applyAirFields(MasterBlAir domain, MasterBlAirJpaEntity jpa) {
        jpa.setAirlineCode(domain.getAirlineCode());
        jpa.setDepartureCode(domain.getDepartureCode());
        jpa.setMawbNo(domain.getMawbNo());
        jpa.setChargeWeightKg(domain.getChargeWeightKg());
        jpa.setVolumeWeightKg(domain.getVolumeWeightKg());
        jpa.setRateClass(domain.getRateClass());
        jpa.setCurrencyCode(domain.getCurrencyCode());
        jpa.setDeclaredValueCarriage(domain.getDeclaredValueCarriage());
        jpa.setDeclaredValueCustoms(domain.getDeclaredValueCustoms());
        jpa.setInsurance(domain.getInsurance());
        jpa.setAccountInformation(domain.getAccountInformation());
        jpa.setSecurityStatus(domain.getSecurityStatus());
        jpa.setFlightType(domain.getFlightType());
        jpa.setIssueDate(domain.getIssueDate());
        jpa.setIssuePlace(domain.getIssuePlace());
        jpa.setSignature(domain.getSignature());
    }
}
