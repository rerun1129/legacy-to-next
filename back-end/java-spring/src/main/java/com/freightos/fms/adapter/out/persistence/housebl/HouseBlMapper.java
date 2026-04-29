package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.ContainerType;
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
        domain.assignHblNo(BlNumber.of(jpa.getHblNo()));
        domain.updateSchedule(PortCode.of(jpa.getPolCode()), PortCode.of(jpa.getPodCode()),
                BlDate.of(jpa.getEtd()), BlDate.of(jpa.getEta()));
        domain.assignOperator(PartyCode.of(jpa.getActualCustomerCode()), EmployeeCode.of(jpa.getOperatorCode()),
                TeamCode.of(jpa.getTeamCode()), EmployeeCode.of(jpa.getSalesManCode()));
        domain.updateBlStatus(jpa.getShipmentType(), jpa.getBlType(), jpa.getFreightTerm());
        domain.assignParties(PartyCode.of(jpa.getShipperCode()), PartyCode.of(jpa.getConsigneeCode()),
                PartyCode.of(jpa.getNotifyCode()), PartyCode.of(jpa.getDocPartnerCode()),
                PortCode.of(jpa.getDeliveryCode()));
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()), jpa.getPkgUnit(),
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm())));
        if (jpa.getMasterBlId() != null) {
            domain.linkToMaster(jpa.getMasterBlId());
        }
    }

    private void copySeaFields(HouseBlSeaJpaEntity jpa, HouseBlSea domain) {
        domain.updateSeaSchedule(PartyCode.of(jpa.getLinerCode()),
                VesselVoyage.of(jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getOnboardDate()));
        domain.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                PortCode.of(jpa.getPorCode()), PortCode.of(jpa.getFinalDestCode()),
                BlDate.of(jpa.getIssueDate()), jpa.getNoOfBl(), jpa.getIssuePlace(),
                BlDate.of(jpa.getDoDate()), jpa.getIncoterms(), jpa.getPayableAt(),
                jpa.isTriangle(), jpa.isCoLoad(), BlNumber.of(jpa.getMblNo()), jpa.getLoadType()));
    }

    private void copyAirFields(HouseBlAirJpaEntity jpa, HouseBlAir domain) {
        domain.updateAirFields(new HouseBlAir.AirFields(
                PartyCode.of(jpa.getAirlineCode()), AirportCode.of(jpa.getDepartureCode()),
                BlNumber.of(jpa.getMawbNo()),
                Weight.of(jpa.getChargeWeightKg()), Weight.of(jpa.getVolumeWeightKg()),
                jpa.getRateClass(), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(), jpa.getOtherTerm(),
                BlDate.of(jpa.getIssueDate()), jpa.getIssuePlace(), jpa.getSignature(),
                jpa.getFhd(), jpa.getIncoterms(), jpa.getFreightTermAir()));
    }

    private void copyTruckFields(HouseBlTruckJpaEntity jpa, HouseBlTruck domain) {
        domain.updateTruckFields(BlDate.of(jpa.getPickupDate()), PartyCode.of(jpa.getTruckerCode()),
                EmployeeCode.of(jpa.getTruckerPic()), Weight.of(jpa.getChargeWeightKg()), jpa.getIncoterms());
    }

    private void copyNonBlFields(HouseBlNonBlJpaEntity jpa, HouseBlNonBl domain) {
        domain.updateNonBlFields(jpa.getSettlePartnerCode(), jpa.getStatus(), jpa.getOriginalBlRef());
    }

    private HouseBlContainer toContainerDomain(HouseBlContainerJpaEntity jpa, HouseBl parent) {
        HouseBlContainer c = HouseBlContainer.of(parent, ContainerNumber.of(jpa.getContainerNo()),
                ContainerType.fromCode(jpa.getContainerType()), jpa.getLengthFeet());
        c.assignIdentity(jpa.getHouseBlContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(new HouseBlContainer.Details(
                SealNumber.of(jpa.getSealNo1()), SealNumber.of(jpa.getSealNo2()),
                Quantity.of(jpa.getPkgQty()), jpa.getPkgUnit(),
                Weight.of(jpa.getGrossWeightKg()), Weight.of(jpa.getNetWeightKg()),
                Volume.of(jpa.getCbm()), Weight.of(jpa.getVgmKg()), jpa.isSoc(), jpa.getSeq()));
        return c;
    }

    // ── Domain → JpaEntity (PersistenceAdapter에서 호출) ──────────

    public void applyCommonFields(HouseBl domain, HouseBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setHouseBlId(domain.getId());
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(switch (domain) {
            case HouseBlSea    ignored -> JobDiv.SEA;
            case HouseBlAir    ignored -> JobDiv.AIR;
            case HouseBlTruck  ignored -> JobDiv.TRUCK;
            case HouseBlNonBl  ignored -> JobDiv.NON_BL;
            default -> throw new IllegalStateException("Unknown HouseBl subtype: " + domain.getClass().getSimpleName());
        });
        jpa.setHblNo(domain.getHblNo() != null ? domain.getHblNo().value() : null);
        jpa.setPolCode(domain.getPolCode() != null ? domain.getPolCode().value() : null);
        jpa.setPodCode(domain.getPodCode() != null ? domain.getPodCode().value() : null);
        jpa.setEtd(domain.getEtd() != null ? domain.getEtd().asString() : null);
        jpa.setEta(domain.getEta() != null ? domain.getEta().asString() : null);
        jpa.setActualCustomerCode(domain.getActualCustomerCode() != null ? domain.getActualCustomerCode().value() : null);
        jpa.setOperatorCode(domain.getOperatorCode() != null ? domain.getOperatorCode().value() : null);
        jpa.setTeamCode(domain.getTeamCode() != null ? domain.getTeamCode().value() : null);
        jpa.setSalesManCode(domain.getSalesManCode() != null ? domain.getSalesManCode().value() : null);
        jpa.setMasterBlId(domain.getMasterBlId());
        jpa.setShipmentType(domain.getShipmentType());
        jpa.setBlType(domain.getBlType());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setShipperCode(domain.getShipperCode() != null ? domain.getShipperCode().value() : null);
        jpa.setConsigneeCode(domain.getConsigneeCode() != null ? domain.getConsigneeCode().value() : null);
        jpa.setNotifyCode(domain.getNotifyCode() != null ? domain.getNotifyCode().value() : null);
        jpa.setDocPartnerCode(domain.getDocPartnerCode() != null ? domain.getDocPartnerCode().value() : null);
        jpa.setDeliveryCode(domain.getDeliveryCode() != null ? domain.getDeliveryCode().value() : null);
        jpa.setPkgQty(domain.getPkgQty() != null ? domain.getPkgQty().count() : null);
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setGrossWeightKg(domain.getGrossWeightKg() != null ? domain.getGrossWeightKg().kg() : null);
        jpa.setCbm(domain.getCbm() != null ? domain.getCbm().cbm() : null);
        // 컨테이너는 SEA 전용, HouseBlPersistenceAdapter에서 처리 (여기서는 common 필드만)
    }

    public void applySeaFields(HouseBlSea domain, HouseBlSeaJpaEntity jpa) {
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(domain.getLinerCode() != null ? domain.getLinerCode().value() : null);
        if (domain.getVesselVoyage() != null) {
            jpa.setVesselName(domain.getVesselVoyage().vesselName());
            jpa.setVoyageNo(domain.getVesselVoyage().voyageNo());
        }
        jpa.setOnboardDate(domain.getOnboardDate() != null ? domain.getOnboardDate().asString() : null);
        jpa.setPorCode(domain.getPorCode() != null ? domain.getPorCode().value() : null);
        jpa.setFinalDestCode(domain.getFinalDestCode() != null ? domain.getFinalDestCode().value() : null);
        jpa.setIssueDate(domain.getIssueDate() != null ? domain.getIssueDate().asString() : null);
        jpa.setNoOfBl(domain.getNoOfBl());
        jpa.setIssuePlace(domain.getIssuePlace());
        jpa.setDoDate(domain.getDoDate() != null ? domain.getDoDate().asString() : null);
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setPayableAt(domain.getPayableAt());
        jpa.setIsTriangle(domain.isTriangle());
        jpa.setIsCoLoad(domain.isCoLoad());
        jpa.setMblNo(domain.getMblNo() != null ? domain.getMblNo().value() : null);
    }

    public void applyAirFields(HouseBlAir domain, HouseBlAirJpaEntity jpa) {
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
        jpa.setOtherTerm(domain.getOtherTerm());
        jpa.setIssueDate(domain.getIssueDate() != null ? domain.getIssueDate().asString() : null);
        jpa.setIssuePlace(domain.getIssuePlace());
        jpa.setSignature(domain.getSignature());
        jpa.setFhd(domain.getFhd());
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setFreightTermAir(domain.getFreightTermAir());
    }

    public void applyTruckFields(HouseBlTruck domain, HouseBlTruckJpaEntity jpa) {
        jpa.setVesselName(domain.getVesselName());
        jpa.setPickupDate(domain.getPickupDate() != null ? domain.getPickupDate().asString() : null);
        jpa.setTruckerCode(domain.getTruckerCode() != null ? domain.getTruckerCode().value() : null);
        jpa.setTruckerPic(domain.getTruckerPic() != null ? domain.getTruckerPic().value() : null);
        jpa.setChargeWeightKg(domain.getChargeWeightKg() != null ? domain.getChargeWeightKg().kg() : null);
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
                c.getContainerNo() != null ? c.getContainerNo().value() : null,
                c.getContainerType() != null ? c.getContainerType().getCode() : null,
                c.getLengthFeet());
        if (c.getId() != null) jpa.setHouseBlContainerId(c.getId());
        jpa.setSealNo1(c.getSealNo1() != null ? c.getSealNo1().value() : null);
        jpa.setSealNo2(c.getSealNo2() != null ? c.getSealNo2().value() : null);
        jpa.setPkgQty(c.getPkgQty() != null ? c.getPkgQty().count() : null);
        jpa.setPkgUnit(c.getPkgUnit());
        jpa.setGrossWeightKg(c.getGrossWeightKg() != null ? c.getGrossWeightKg().kg() : null);
        jpa.setNetWeightKg(c.getNetWeightKg() != null ? c.getNetWeightKg().kg() : null);
        jpa.setCbm(c.getCbm() != null ? c.getCbm().cbm() : null);
        jpa.setVgmKg(c.getVgmKg() != null ? c.getVgmKg().kg() : null);
        jpa.setIsSoc(c.isSoc());
        jpa.setSeq(c.getSeq());
        return jpa;
    }
}
