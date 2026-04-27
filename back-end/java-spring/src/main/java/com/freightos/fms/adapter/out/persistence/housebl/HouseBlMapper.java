package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.housebl.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

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
        if (jpa.getMasterBlId() != null) {
            domain.linkToMaster(jpa.getMasterBlId());
        }
    }

    private void copyAirFields(HouseBlAirJpaEntity jpa, HouseBlAir domain) {
        // 리플렉션 없이 필드 복사는 도메인 setter가 없으므로 불필요한 setter 노출을 피한다.
        // 현재 구조에서 도메인은 create() 이후 추가 수정 메서드가 없으므로
        // domain 계층에 필요 시 update 메서드를 추가하는 방향으로 확장한다.
        // 현재는 기본 필드 복사만 수행 (추후 updateAir 메서드 추가로 확장 가능).
    }

    private void copySeaFields(HouseBlSeaJpaEntity jpa, HouseBlSea domain) {
        domain.updateSeaSchedule(jpa.getLinerCode(), jpa.getVesselName(),
                jpa.getVoyageNo(), jpa.getOnboardDate());
    }

    private void copyTruckFields(HouseBlTruckJpaEntity jpa, HouseBlTruck domain) {
        // 트럭 전용 필드 복사 (도메인 update 메서드 추가 시 확장)
    }

    private void copyNonBlFields(HouseBlNonBlJpaEntity jpa, HouseBlNonBl domain) {
        // NonBl 전용 필드 복사 (도메인 update 메서드 추가 시 확장)
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
