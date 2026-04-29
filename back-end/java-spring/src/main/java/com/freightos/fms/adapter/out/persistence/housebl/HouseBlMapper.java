package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.FreightCondition;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.PackageUnit;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.housebl.enums.Fhd;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.freightos.fms.common.util.VoMapper.mapOrNull;

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
        domain.assignOperator(CustomerCode.of(jpa.getActualCustomerCode()), EmployeeCode.of(jpa.getOperatorCode()),
                TeamCode.of(jpa.getTeamCode()), EmployeeCode.of(jpa.getSalesManCode()));
        domain.updateBlStatus(jpa.getShipmentType(), jpa.getBlType(), jpa.getFreightTerm());
        domain.assignParties(CustomerCode.of(jpa.getShipperCode()), CustomerCode.of(jpa.getConsigneeCode()),
                CustomerCode.of(jpa.getNotifyCode()), CustomerCode.of(jpa.getDocPartnerCode()),
                PortCode.of(jpa.getDeliveryCode()));
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()), PackageUnit.fromCode(jpa.getPkgUnit()),
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm())));
        if (jpa.getMasterBlId() != null) domain.linkToMaster(jpa.getMasterBlId());
    }

    private void copySeaFields(HouseBlSeaJpaEntity jpa, HouseBlSea domain) {
        domain.updateSeaSchedule(LinerCode.of(jpa.getLinerCode()),
                VesselVoyage.of(jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getOnboardDate()));
        domain.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                PortCode.of(jpa.getPorCode()), PortCode.of(jpa.getFinalDestCode()),
                BlDate.of(jpa.getIssueDate()), NoOfBl.fromNumber(jpa.getNoOfBl()), PortCode.of(jpa.getIssuePlace()),
                BlDate.of(jpa.getDoDate()), Incoterms.fromCode(jpa.getIncoterms()), PortCode.of(jpa.getPayableAt()),
                jpa.isTriangle(), jpa.isCoLoad(), BlNumber.of(jpa.getMblNo()),
                jpa.getLoadType(), FreightCondition.fromCode(jpa.getFreightTermSea())));
    }

    private void copyAirFields(HouseBlAirJpaEntity jpa, HouseBlAir domain) {
        domain.updateAirFields(new HouseBlAir.AirFields(
                AirlineCode.of(jpa.getAirlineCode()), AirportCode.of(jpa.getDepartureCode()),
                BlNumber.of(jpa.getMawbNo()),
                Weight.of(jpa.getChargeWeightKg()), Weight.of(jpa.getVolumeWeightKg()),
                RateClass.fromCode(jpa.getRateClass()), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(), FreightTerm.fromCode(jpa.getOtherTerm()),
                BlDate.of(jpa.getIssueDate()), PortCode.of(jpa.getIssuePlace()), jpa.getSignature(),
                Fhd.fromCode(jpa.getFhd()), Incoterms.fromCode(jpa.getIncoterms()),
                FreightCondition.fromCode(jpa.getFreightTermAir())));
    }

    private void copyTruckFields(HouseBlTruckJpaEntity jpa, HouseBlTruck domain) {
        domain.updateTruckFields(BlDate.of(jpa.getPickupDate()), CustomerCode.of(jpa.getTruckerCode()),
                EmployeeCode.of(jpa.getTruckerPic()), Weight.of(jpa.getChargeWeightKg()),
                Incoterms.fromCode(jpa.getIncoterms()));
    }

    private void copyNonBlFields(HouseBlNonBlJpaEntity jpa, HouseBlNonBl domain) {
        domain.updateNonBlFields(CustomerCode.of(jpa.getSettlePartnerCode()), BlNumber.of(jpa.getOriginalBlRef()));
    }

    private HouseBlContainer toContainerDomain(HouseBlContainerJpaEntity jpa, HouseBl parent) {
        HouseBlContainer c = HouseBlContainer.of(parent, ContainerNumber.of(jpa.getContainerNo()),
                ContainerType.fromCode(jpa.getContainerType()), jpa.getLengthFeet());
        c.assignIdentity(jpa.getHouseBlContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(new HouseBlContainer.Details(
                SealNumber.of(jpa.getSealNo1()), SealNumber.of(jpa.getSealNo2()),
                Quantity.of(jpa.getPkgQty()), PackageUnit.fromCode(jpa.getPkgUnit()),
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
        jpa.setHblNo(mapOrNull(domain.getHblNo(), BlNumber::value));
        jpa.setPolCode(mapOrNull(domain.getPolCode(), PortCode::value));
        jpa.setPodCode(mapOrNull(domain.getPodCode(), PortCode::value));
        jpa.setEtd(mapOrNull(domain.getEtd(), BlDate::asString));
        jpa.setEta(mapOrNull(domain.getEta(), BlDate::asString));
        jpa.setActualCustomerCode(mapOrNull(domain.getActualCustomerCode(), CustomerCode::value));
        jpa.setOperatorCode(mapOrNull(domain.getOperatorCode(), EmployeeCode::value));
        jpa.setTeamCode(mapOrNull(domain.getTeamCode(), TeamCode::value));
        jpa.setSalesManCode(mapOrNull(domain.getSalesManCode(), EmployeeCode::value));
        jpa.setMasterBlId(domain.getMasterBlId());
        jpa.setShipmentType(domain.getShipmentType());
        jpa.setBlType(domain.getBlType());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setShipperCode(mapOrNull(domain.getShipperCode(), CustomerCode::value));
        jpa.setConsigneeCode(mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
        jpa.setNotifyCode(mapOrNull(domain.getNotifyCode(), CustomerCode::value));
        jpa.setDocPartnerCode(mapOrNull(domain.getDocPartnerCode(), CustomerCode::value));
        jpa.setDeliveryCode(mapOrNull(domain.getDeliveryCode(), PortCode::value));
        jpa.setPkgQty(mapOrNull(domain.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(mapOrNull(domain.getPkgUnit(), PackageUnit::name));
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        // 컨테이너는 SEA 전용, HouseBlPersistenceAdapter에서 처리 (여기서는 common 필드만)
    }

    public void applySeaFields(HouseBlSea domain, HouseBlSeaJpaEntity jpa) {
        jpa.setLoadType(domain.getLoadType());
        jpa.setLinerCode(mapOrNull(domain.getLinerCode(), LinerCode::value));
        if (domain.getVesselVoyage() != null) {
            jpa.setVesselName(domain.getVesselVoyage().vesselName());
            jpa.setVoyageNo(domain.getVesselVoyage().voyageNo());
        }
        jpa.setOnboardDate(mapOrNull(domain.getOnboardDate(), BlDate::asString));
        jpa.setPorCode(mapOrNull(domain.getPorCode(), PortCode::value));
        jpa.setFinalDestCode(mapOrNull(domain.getFinalDestCode(), PortCode::value));
        jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
        jpa.setNoOfBl(mapOrNull(domain.getNoOfBl(), NoOfBl::getNumber));
        jpa.setIssuePlace(mapOrNull(domain.getIssuePlace(), PortCode::value));
        jpa.setDoDate(mapOrNull(domain.getDoDate(), BlDate::asString));
        jpa.setIncoterms(mapOrNull(domain.getIncoterms(), Incoterms::name));
        jpa.setPayableAt(mapOrNull(domain.getPayableAt(), PortCode::value));
        jpa.setIsTriangle(domain.isTriangle());
        jpa.setIsCoLoad(domain.isCoLoad());
        jpa.setMblNo(mapOrNull(domain.getMblNo(), BlNumber::value));
        jpa.setFreightTermSea(mapOrNull(domain.getFreightTermSea(), FreightCondition::name));
    }

    public void applyAirFields(HouseBlAir domain, HouseBlAirJpaEntity jpa) {
        jpa.setAirlineCode(mapOrNull(domain.getAirlineCode(), AirlineCode::value));
        jpa.setDepartureCode(mapOrNull(domain.getDepartureCode(), AirportCode::value));
        jpa.setMawbNo(mapOrNull(domain.getMawbNo(), BlNumber::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setVolumeWeightKg(mapOrNull(domain.getVolumeWeightKg(), Weight::kg));
        jpa.setRateClass(mapOrNull(domain.getRateClass(), RateClass::name));
        jpa.setCurrencyCode(mapOrNull(domain.getCurrencyCode(), CurrencyCode::value));
        jpa.setDeclaredValueCarriage(domain.getDeclaredValueCarriage());
        jpa.setDeclaredValueCustoms(domain.getDeclaredValueCustoms());
        jpa.setInsurance(domain.getInsurance());
        jpa.setAccountInformation(domain.getAccountInformation());
        jpa.setOtherTerm(mapOrNull(domain.getOtherTerm(), FreightTerm::name));
        jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
        jpa.setIssuePlace(mapOrNull(domain.getIssuePlace(), PortCode::value));
        jpa.setSignature(domain.getSignature());
        jpa.setFhd(mapOrNull(domain.getFhd(), Fhd::name));
        jpa.setIncoterms(mapOrNull(domain.getIncoterms(), Incoterms::name));
        jpa.setFreightTermAir(mapOrNull(domain.getFreightTermAir(), FreightCondition::name));
    }

    public void applyTruckFields(HouseBlTruck domain, HouseBlTruckJpaEntity jpa) {
        jpa.setVesselName(domain.getVesselName());
        jpa.setPickupDate(mapOrNull(domain.getPickupDate(), BlDate::asString));
        jpa.setTruckerCode(mapOrNull(domain.getTruckerCode(), CustomerCode::value));
        jpa.setTruckerPic(mapOrNull(domain.getTruckerPic(), EmployeeCode::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setIncoterms(mapOrNull(domain.getIncoterms(), Incoterms::name));
    }

    public void applyNonBlFields(HouseBlNonBl domain, HouseBlNonBlJpaEntity jpa) {
        jpa.setWorkDivision(domain.getWorkDivision());
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setOriginalBlRef(mapOrNull(domain.getOriginalBlRef(), BlNumber::value));
    }

    HouseBlContainerJpaEntity toContainerJpa(HouseBlContainer c, HouseBlJpaEntity jpaParent) {
        HouseBlContainerJpaEntity jpa = HouseBlContainerJpaEntity.of(jpaParent,
                mapOrNull(c.getContainerNo(), ContainerNumber::value),
                mapOrNull(c.getContainerType(), ContainerType::getCode),
                c.getLengthFeet());
        if (c.getId() != null) jpa.setHouseBlContainerId(c.getId());
        jpa.setSealNo1(mapOrNull(c.getSealNo1(), SealNumber::value));
        jpa.setSealNo2(mapOrNull(c.getSealNo2(), SealNumber::value));
        jpa.setPkgQty(mapOrNull(c.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(mapOrNull(c.getPkgUnit(), PackageUnit::name));
        jpa.setGrossWeightKg(mapOrNull(c.getGrossWeightKg(), Weight::kg));
        jpa.setNetWeightKg(mapOrNull(c.getNetWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(c.getCbm(), Volume::cbm));
        jpa.setVgmKg(mapOrNull(c.getVgmKg(), Weight::kg));
        jpa.setIsSoc(c.isSoc());
        jpa.setSeq(c.getSeq());
        return jpa;
    }
}
