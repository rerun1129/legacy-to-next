package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.*;
import com.freightos.fms.domain.masterbl.entity.*;
import org.springframework.stereotype.Component;

/**
 * JPA ↔ Domain 변환 매퍼 — Master B/L.
 */
@Component
public class MasterBlMapper {

    // ── JpaEntity → Domain ─────────────────────────────────────────

    public MasterBl toDomain(MasterBlJpaEntity jpa) {
        if (jpa instanceof MasterBlAirJpaEntity air) {
            return toAirDomain(air);
        } else if (jpa instanceof MasterBlSeaJpaEntity sea) {
            return toSeaDomain(sea);
        }
        throw new IllegalArgumentException("Unknown MasterBlJpaEntity type: " + jpa.getClass());
    }

    private MasterBlAir toAirDomain(MasterBlAirJpaEntity jpa) {
        MasterBlAir domain = MasterBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        copyAirFields(jpa, domain);
        return domain;
    }

    private MasterBlSea toSeaDomain(MasterBlSeaJpaEntity jpa) {
        MasterBlSea domain = MasterBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        copySeaFields(jpa, domain);
        return domain;
    }

    private void copyBaseFields(MasterBlJpaEntity jpa, MasterBl domain) {
        domain.assignMblNo(jpa.getMblNo(), jpa.getMasterRefNo());
        domain.assignParties(jpa.getShipperCode(), jpa.getConsigneeCode(), jpa.getNotifyCode());
        domain.updateSchedule(jpa.getPolCode(), jpa.getPodCode(), jpa.getEtd(), jpa.getEta());
        domain.updateFreightAndOperator(jpa.getFreightTerm(), jpa.getOperatorCode(), jpa.getTeamCode());
        domain.updateCargoSummary(jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getCbm());
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

    private void copySeaFields(MasterBlSeaJpaEntity jpa, MasterBlSea domain) {
        domain.updateSeaFields(jpa.getLoadType(), jpa.getLinerCode(), jpa.getVesselName(),
                jpa.getVoyageNo(), jpa.getOnboardDate(),
                jpa.getLineBkgNo(), jpa.getIssueDate());
    }

    // ── Domain → JpaEntity ─────────────────────────────────────────

    public MasterBlJpaEntity toJpa(MasterBl domain) {
        if (domain instanceof MasterBlAir air) {
            return fromAirDomain(air);
        } else if (domain instanceof MasterBlSea sea) {
            return fromSeaDomain(sea);
        }
        throw new IllegalArgumentException("Unknown MasterBl domain type: " + domain.getClass());
    }

    private MasterBlAirJpaEntity fromAirDomain(MasterBlAir domain) {
        MasterBlAirJpaEntity jpa = new MasterBlAirJpaEntity();
        applyBaseFields(domain, jpa);
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
        return jpa;
    }

    private MasterBlSeaJpaEntity fromSeaDomain(MasterBlSea domain) {
        MasterBlSeaJpaEntity jpa = new MasterBlSeaJpaEntity();
        applyBaseFields(domain, jpa);
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(domain.getLinerCode());
        jpa.setVesselName(domain.getVesselName());
        jpa.setVoyageNo(domain.getVoyageNo());
        jpa.setOnboardDate(domain.getOnboardDate());
        jpa.setLineBkgNo(domain.getLineBkgNo());
        jpa.setIssueDate(domain.getIssueDate());
        return jpa;
    }

    private void applyBaseFields(MasterBl domain, MasterBlJpaEntity jpa) {
        jpa.setMblNo(domain.getMblNo());
        jpa.setMasterRefNo(domain.getMasterRefNo());
        jpa.setBound(domain.getBound());
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
}
