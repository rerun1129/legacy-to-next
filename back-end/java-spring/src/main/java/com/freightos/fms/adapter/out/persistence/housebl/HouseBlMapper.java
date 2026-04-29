package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA ↔ Domain 변환 매퍼 — House B/L.
 * toDomain: JobDiv enum switch로 분기 (instanceof 패턴 제거).
 * applyXxxFields: PersistenceAdapter에서 직접 호출.
 */
@Component
public class HouseBlMapper {

    // ── JpaEntity → Domain ─────────────────────────────────────────

    public HouseBl toDomain(HouseBlJpaEntity jpa) {
        JobDiv jobDiv = jpa.getJobDiv();
        return switch (jobDiv) {
            case SEA    -> toSeaDomain(jpa, jpa.getSeaExt());
            case AIR    -> toAirDomain(jpa, jpa.getAirExt());
            case TRUCK  -> toTruckDomain(jpa, jpa.getTruckExt());
            case NON_BL -> toNonBlDomain(jpa, jpa.getNonBlExt());
        };
    }

    private HouseBlSea toSeaDomain(HouseBlJpaEntity jpa, HouseBlSeaJpaEntity seaJpa) {
        HouseBlSea domain = HouseBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (seaJpa != null) copySeaFields(seaJpa, domain);
        // SEA만 컨테이너 보유 — @BatchSize(50)으로 리스트 조회 시 batch fetch
        List<HouseBlContainer> containers = jpa.getContainers().stream()
                .map(c -> toContainerDomain(c, domain))
                .collect(Collectors.toList());
        domain.initContainers(containers);
        return domain;
    }

    private HouseBlAir toAirDomain(HouseBlJpaEntity jpa, HouseBlAirJpaEntity airJpa) {
        HouseBlAir domain = HouseBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (airJpa != null) copyAirFields(airJpa, domain);
        return domain;
    }

    private HouseBlTruck toTruckDomain(HouseBlJpaEntity jpa, HouseBlTruckJpaEntity truckJpa) {
        HouseBlTruck domain = HouseBlTruck.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (truckJpa != null) copyTruckFields(truckJpa, domain);
        return domain;
    }

    private HouseBlNonBl toNonBlDomain(HouseBlJpaEntity jpa, HouseBlNonBlJpaEntity nonBlJpa) {
        HouseBlNonBl domain = HouseBlNonBl.create(nonBlJpa != null ? nonBlJpa.getWorkDivision() : null, jpa.getBound());
        copyBaseFields(jpa, domain);
        if (nonBlJpa != null) copyNonBlFields(nonBlJpa, domain);
        return domain;
    }

    private void copyBaseFields(HouseBlJpaEntity jpa, HouseBl domain) {
        domain.assignIdentity(jpa.getHouseBlId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
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

    private void copySeaFields(HouseBlSeaJpaEntity jpa, HouseBlSea domain) {
        domain.updateSeaSchedule(jpa.getLinerCode(), jpa.getVesselName(),
                jpa.getVoyageNo(), jpa.getOnboardDate());
        domain.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                jpa.getPorCode(), jpa.getFinalDestCode(),
                jpa.getIssueDate(), jpa.getNoOfBl(), jpa.getIssuePlace(),
                jpa.getDoDate(), jpa.getIncoterms(), jpa.getPayableAt(),
                jpa.isTriangle(), jpa.isCoLoad(), jpa.getMblNo(), jpa.getLoadType()));
    }

    private void copyAirFields(HouseBlAirJpaEntity jpa, HouseBlAir domain) {
        domain.updateAirFields(new HouseBlAir.AirFields(
                jpa.getAirlineCode(), jpa.getDepartureCode(), jpa.getMawbNo(),
                jpa.getChargeWeightKg(), jpa.getVolumeWeightKg(),
                jpa.getRateClass(), jpa.getCurrencyCode(),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(), jpa.getOtherTerm(),
                jpa.getIssueDate(), jpa.getIssuePlace(), jpa.getSignature(),
                jpa.getFhd(), jpa.getIncoterms(), jpa.getFreightTermAir()));
    }

    private void copyTruckFields(HouseBlTruckJpaEntity jpa, HouseBlTruck domain) {
        domain.updateTruckFields(jpa.getPickupDate(), jpa.getTruckerCode(), jpa.getTruckerPic(),
                jpa.getChargeWeightKg(), jpa.getIncoterms());
    }

    private void copyNonBlFields(HouseBlNonBlJpaEntity jpa, HouseBlNonBl domain) {
        domain.updateNonBlFields(jpa.getSettlePartnerCode(), jpa.getStatus(),
                jpa.getOriginalBlRef());
    }

    private HouseBlContainer toContainerDomain(HouseBlContainerJpaEntity jpa, HouseBl parent) {
        HouseBlContainer c = HouseBlContainer.of(parent, jpa.getContainerNo(),
                jpa.getContainerType(), jpa.getLengthFeet());
        c.assignIdentity(jpa.getHouseBlContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(new HouseBlContainer.Details(
                jpa.getSealNo1(), jpa.getSealNo2(), jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getNetWeightKg(), jpa.getCbm(),
                jpa.getVgmKg(), jpa.isSoc(), jpa.getSeq()));
        return c;
    }

    // ── Domain → JpaEntity (PersistenceAdapter에서 호출) ──────────

    public void applyCommonFields(HouseBl domain, HouseBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setHouseBlId(domain.getId());
        jpa.setBound(domain.getBound());
        // jobDiv는 domain 클래스 타입에서 판별
        jpa.setJobDiv(switch (domain) {
            case HouseBlSea    ignored -> JobDiv.SEA;
            case HouseBlAir    ignored -> JobDiv.AIR;
            case HouseBlTruck  ignored -> JobDiv.TRUCK;
            case HouseBlNonBl  ignored -> JobDiv.NON_BL;
            default -> throw new IllegalStateException("Unknown HouseBl subtype: " + domain.getClass().getSimpleName());
        });
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
        // 컨테이너는 SEA 전용, HouseBlPersistenceAdapter에서 처리 (여기서는 common 필드만)
    }

    public void applySeaFields(HouseBlSea domain, HouseBlSeaJpaEntity jpa) {
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
    }

    public void applyAirFields(HouseBlAir domain, HouseBlAirJpaEntity jpa) {
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
    }

    public void applyTruckFields(HouseBlTruck domain, HouseBlTruckJpaEntity jpa) {
        jpa.setVesselName(domain.getVesselName());
        jpa.setPickupDate(domain.getPickupDate());
        jpa.setTruckerCode(domain.getTruckerCode());
        jpa.setTruckerPic(domain.getTruckerPic());
        jpa.setChargeWeightKg(domain.getChargeWeightKg());
        jpa.setIncoterms(domain.getIncoterms());
    }

    public void applyNonBlFields(HouseBlNonBl domain, HouseBlNonBlJpaEntity jpa) {
        jpa.setWorkDivision(domain.getWorkDivision());
        jpa.setSettlePartnerCode(domain.getSettlePartnerCode());
        jpa.setStatus(domain.getStatus());
        jpa.setOriginalBlRef(domain.getOriginalBlRef());
    }

    HouseBlContainerJpaEntity toContainerJpa(HouseBlContainer c, HouseBlJpaEntity jpaParent) {
        HouseBlContainerJpaEntity jpa = HouseBlContainerJpaEntity.of(jpaParent,
                c.getContainerNo(), c.getContainerType(), c.getLengthFeet());
        if (c.getId() != null) jpa.setHouseBlContainerId(c.getId());
        jpa.setSealNo1(c.getSealNo1());
        jpa.setSealNo2(c.getSealNo2());
        jpa.setPkgQty(c.getPkgQty());
        jpa.setPkgUnit(c.getPkgUnit());
        jpa.setGrossWeightKg(c.getGrossWeightKg());
        jpa.setNetWeightKg(c.getNetWeightKg());
        jpa.setCbm(c.getCbm());
        jpa.setVgmKg(c.getVgmKg());
        jpa.setIsSoc(c.isSoc());
        jpa.setSeq(c.getSeq());
        return jpa;
    }
}
