package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.housebl.entity.*;
import org.springframework.stereotype.Component;

/**
 * JPA ↔ Domain 변환 매퍼.
 * Domain → JpaEntity 및 JpaEntity → Domain 변환 메서드 제공.
 */
@Component
public class HouseBlMapper {

    // ── JpaEntity → Domain ─────────────────────────────────────────

    public HouseBl toDomain(HouseBlJpaEntity jpa) {
        if (jpa instanceof HouseBlAirJpaEntity air) {
            return toAirDomain(air);
        } else if (jpa instanceof HouseBlSeaJpaEntity sea) {
            return toSeaDomain(sea);
        } else if (jpa instanceof HouseBlTruckJpaEntity truck) {
            return toTruckDomain(truck);
        } else if (jpa instanceof HouseBlNonBlJpaEntity nonBl) {
            return toNonBlDomain(nonBl);
        }
        throw new IllegalArgumentException("Unknown HouseBlJpaEntity type: " + jpa.getClass());
    }

    private HouseBlAir toAirDomain(HouseBlAirJpaEntity jpa) {
        HouseBlAir domain = HouseBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        copyAirFields(jpa, domain);
        return domain;
    }

    private HouseBlSea toSeaDomain(HouseBlSeaJpaEntity jpa) {
        HouseBlSea domain = HouseBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        copySeaFields(jpa, domain);
        return domain;
    }

    private HouseBlTruck toTruckDomain(HouseBlTruckJpaEntity jpa) {
        HouseBlTruck domain = HouseBlTruck.create();
        copyBaseFields(jpa, domain);
        copyTruckFields(jpa, domain);
        return domain;
    }

    private HouseBlNonBl toNonBlDomain(HouseBlNonBlJpaEntity jpa) {
        HouseBlNonBl domain = HouseBlNonBl.create(jpa.getWorkDivision());
        copyBaseFields(jpa, domain);
        copyNonBlFields(jpa, domain);
        return domain;
    }

    private void copyBaseFields(HouseBlJpaEntity jpa, HouseBl domain) {
        domain.assignHblNo(jpa.getHblNo());
        domain.updateSchedule(jpa.getPolCode(), jpa.getPodCode(), jpa.getEtd(), jpa.getEta());
        domain.assignOperator(jpa.getActualCustomerCode(), jpa.getOperatorCode(),
                jpa.getTeamCode(), jpa.getSalesManCode());
        domain.updateBlStatus(jpa.getShipmentType(), jpa.getBlType(), jpa.getFreightTerm());
        domain.assignParties(jpa.getShipperCode(), jpa.getConsigneeCode(), jpa.getNotifyCode(),
                jpa.getDocPartnerCode(), jpa.getDeliveryCode());
        domain.updateCargoSummary(jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getCbm());
        if (jpa.getMasterBlId() != null) {
            domain.linkToMaster(jpa.getMasterBlId());
        }
    }

    private void copyAirFields(HouseBlAirJpaEntity jpa, HouseBlAir domain) {
        domain.updateAirFields(
                jpa.getAirlineCode(), jpa.getDepartureCode(), jpa.getMawbNo(),
                jpa.getChargeWeightKg(), jpa.getVolumeWeightKg(),
                jpa.getRateClass(), jpa.getCurrencyCode(),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(), jpa.getOtherTerm(),
                jpa.getIssueDate(), jpa.getIssuePlace(), jpa.getSignature(),
                jpa.getFhd(), jpa.getIncoterms(), jpa.getFreightTermAir());
    }

    private void copySeaFields(HouseBlSeaJpaEntity jpa, HouseBlSea domain) {
        domain.updateSeaSchedule(jpa.getLinerCode(), jpa.getVesselName(),
                jpa.getVoyageNo(), jpa.getOnboardDate());
        domain.updateSeaRouteAndFlags(
                jpa.getPorCode(), jpa.getFinalDestCode(),
                jpa.getIssueDate(), jpa.getNoOfBl(), jpa.getIssuePlace(),
                jpa.getDoDate(), jpa.getIncoterms(), jpa.getPayableAt(),
                jpa.isTriangle(), jpa.isCoLoad(), jpa.getMblNo(), jpa.getLoadType());
    }

    private void copyTruckFields(HouseBlTruckJpaEntity jpa, HouseBlTruck domain) {
        domain.updateTruckFields(jpa.getPickupDate(), jpa.getTruckerCode(), jpa.getTruckerPic(),
                jpa.getChargeWeightKg(), jpa.getIncoterms());
    }

    private void copyNonBlFields(HouseBlNonBlJpaEntity jpa, HouseBlNonBl domain) {
        domain.updateNonBlFields(jpa.getSettlePartnerCode(), jpa.getStatus(),
                jpa.getOriginalBlRef());
    }

    // ── Domain → JpaEntity ─────────────────────────────────────────

    public HouseBlJpaEntity toJpa(HouseBl domain) {
        if (domain instanceof HouseBlAir air) {
            return fromAirDomain(air);
        } else if (domain instanceof HouseBlSea sea) {
            return fromSeaDomain(sea);
        } else if (domain instanceof HouseBlTruck truck) {
            return fromTruckDomain(truck);
        } else if (domain instanceof HouseBlNonBl nonBl) {
            return fromNonBlDomain(nonBl);
        }
        throw new IllegalArgumentException("Unknown HouseBl domain type: " + domain.getClass());
    }

    private HouseBlAirJpaEntity fromAirDomain(HouseBlAir domain) {
        HouseBlAirJpaEntity jpa = new HouseBlAirJpaEntity();
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
        jpa.setOtherTerm(domain.getOtherTerm());
        jpa.setIssueDate(domain.getIssueDate());
        jpa.setIssuePlace(domain.getIssuePlace());
        jpa.setSignature(domain.getSignature());
        jpa.setFhd(domain.getFhd());
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setFreightTermAir(domain.getFreightTermAir());
        return jpa;
    }

    private HouseBlSeaJpaEntity fromSeaDomain(HouseBlSea domain) {
        HouseBlSeaJpaEntity jpa = new HouseBlSeaJpaEntity();
        applyBaseFields(domain, jpa);
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(domain.getLinerCode());
        jpa.setVesselName(domain.getVesselName());
        jpa.setVoyageNo(domain.getVoyageNo());
        jpa.setOnboardDate(domain.getOnboardDate());
        jpa.setPorCode(domain.getPorCode());
        jpa.setFinalDestCode(domain.getFinalDestCode());
        jpa.setIssueDate(domain.getIssueDate());
        jpa.setNoOfBl(domain.getNoOfBl());
        jpa.setIssuePlace(domain.getIssuePlace());
        jpa.setDoDate(domain.getDoDate());
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setPayableAt(domain.getPayableAt());
        jpa.setIsTriangle(domain.isTriangle());
        jpa.setIsCoLoad(domain.isCoLoad());
        jpa.setMblNo(domain.getMblNo());
        return jpa;
    }

    private HouseBlTruckJpaEntity fromTruckDomain(HouseBlTruck domain) {
        HouseBlTruckJpaEntity jpa = new HouseBlTruckJpaEntity();
        applyBaseFields(domain, jpa);
        jpa.setVesselName(domain.getVesselName());
        jpa.setPickupDate(domain.getPickupDate());
        jpa.setTruckerCode(domain.getTruckerCode());
        jpa.setTruckerPic(domain.getTruckerPic());
        jpa.setChargeWeightKg(domain.getChargeWeightKg());
        jpa.setIncoterms(domain.getIncoterms());
        return jpa;
    }

    private HouseBlNonBlJpaEntity fromNonBlDomain(HouseBlNonBl domain) {
        HouseBlNonBlJpaEntity jpa = new HouseBlNonBlJpaEntity();
        applyBaseFields(domain, jpa);
        jpa.setWorkDivision(domain.getWorkDivision());
        jpa.setSettlePartnerCode(domain.getSettlePartnerCode());
        jpa.setStatus(domain.getStatus());
        jpa.setOriginalBlRef(domain.getOriginalBlRef());
        return jpa;
    }

    private void applyBaseFields(HouseBl domain, HouseBlJpaEntity jpa) {
        jpa.setHblNo(domain.getHblNo());
        jpa.setPolCode(domain.getPolCode());
        jpa.setPodCode(domain.getPodCode());
        jpa.setEtd(domain.getEtd());
        jpa.setEta(domain.getEta());
        jpa.setActualCustomerCode(domain.getActualCustomerCode());
        jpa.setOperatorCode(domain.getOperatorCode());
        jpa.setTeamCode(domain.getTeamCode());
        jpa.setSalesManCode(domain.getSalesManCode());
        jpa.setMasterBlId(domain.getMasterBlId());
        jpa.setShipmentType(domain.getShipmentType());
        jpa.setBlType(domain.getBlType());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setShipperCode(domain.getShipperCode());
        jpa.setConsigneeCode(domain.getConsigneeCode());
        jpa.setNotifyCode(domain.getNotifyCode());
        jpa.setDocPartnerCode(domain.getDocPartnerCode());
        jpa.setDeliveryCode(domain.getDeliveryCode());
        jpa.setPkgQty(domain.getPkgQty());
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setGrossWeightKg(domain.getGrossWeightKg());
        jpa.setCbm(domain.getCbm());
    }
}
