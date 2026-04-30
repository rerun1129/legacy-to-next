package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.PackageUnit;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.housebl.enums.Fhd;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.CargoType;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.HandlingInfoCode;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.LoadType;
import com.freightos.fms.domain.housebl.enums.ServiceTerm;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class HouseBlMapper {

    private final HouseBlCargoMapper cargoMapper;
    private final HouseBlDocMapper docMapper;

    // ── JpaEntity → Domain ─────────────────────────────────────────

    public HouseBlSea toSeaDomain(HouseBlJpaEntity jpa, HouseBlSeaJpaEntity seaJpa) {
        HouseBlSea domain = HouseBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (seaJpa != null) copySeaFields(seaJpa, domain);
        // @BatchSize(50)으로 리스트 조회 시 batch fetch
        List<HouseBlContainer> containers = jpa.getContainers().stream()
                .map(c -> toContainerDomain(c, domain))
                .collect(Collectors.toList());
        domain.initContainers(containers);
        List<HouseBlLicense> licenses = jpa.getLicenses().stream()
                .map(cargoMapper::toLicenseDomain)
                .collect(Collectors.toList());
        domain.initLicenses(licenses);
        if (jpa.getDesc() != null) {
            domain.initDesc(docMapper.toDescDomain(jpa.getDesc()));
        }
        return domain;
    }

    public HouseBlAir toAirDomain(HouseBlJpaEntity jpa, HouseBlAirJpaEntity airJpa) {
        HouseBlAir domain = HouseBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (airJpa != null) copyAirFields(airJpa, domain);
        List<HouseBlDim> dims = jpa.getDims().stream()
                .map(cargoMapper::toDimDomain)
                .collect(Collectors.toList());
        domain.initDims(dims);
        List<HouseBlScheduleLeg> scheduleLegs = jpa.getScheduleLegs().stream()
                .map(docMapper::toScheduleLegDomain)
                .collect(Collectors.toList());
        domain.initScheduleLegs(scheduleLegs);
        List<HouseBlLicense> licenses = jpa.getLicenses().stream()
                .map(cargoMapper::toLicenseDomain)
                .collect(Collectors.toList());
        domain.initLicenses(licenses);
        List<HouseBlAirCharge> airCharges = jpa.getAirCharges().stream()
                .map(docMapper::toAirChargeDomain)
                .collect(Collectors.toList());
        domain.initAirCharges(airCharges);
        if (jpa.getDesc() != null) {
            domain.initDesc(docMapper.toDescDomain(jpa.getDesc()));
        }
        return domain;
    }

    public HouseBlTruck toTruckDomain(HouseBlJpaEntity jpa, HouseBlTruckJpaEntity truckJpa) {
        HouseBlTruck domain = HouseBlTruck.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (truckJpa != null) copyTruckFields(truckJpa, domain);
        List<HouseBlDim> dims = jpa.getDims().stream()
                .map(cargoMapper::toDimDomain)
                .collect(Collectors.toList());
        domain.initDims(dims);
        List<HouseBlTruckOrder> truckOrders = jpa.getTruckOrders().stream()
                .map(docMapper::toTruckOrderDomain)
                .collect(Collectors.toList());
        domain.initTruckOrders(truckOrders);
        return domain;
    }

    public HouseBlNonBl toNonBlDomain(HouseBlJpaEntity jpa, HouseBlNonBlJpaEntity nonBlJpa) {
        HouseBlNonBl domain = HouseBlNonBl.create(nonBlJpa != null ? nonBlJpa.getWorkDivision() : null, jpa.getBound());
        copyBaseFields(jpa, domain);
        if (nonBlJpa != null) copyNonBlFields(nonBlJpa, domain);
        List<HouseBlContainer> containers = jpa.getContainers().stream()
                .map(c -> toContainerDomain(c, domain))
                .collect(Collectors.toList());
        domain.initContainers(containers);
        List<HouseBlDim> dims = jpa.getDims().stream()
                .map(cargoMapper::toDimDomain)
                .collect(Collectors.toList());
        domain.initDims(dims);
        if (jpa.getDesc() != null) {
            domain.initDesc(docMapper.toDescDomain(jpa.getDesc()));
        }
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
        domain.assignParties(
                CustomerCode.of(jpa.getShipperCode(), jpa.getShipperAddress()),
                CustomerCode.of(jpa.getConsigneeCode(), jpa.getConsigneeAddress()),
                CustomerCode.of(jpa.getNotifyCode(), jpa.getNotifyAddress()),
                CustomerCode.of(jpa.getDocPartnerCode(), jpa.getDocPartnerAddress()),
                PortCode.of(jpa.getDeliveryCode()));
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()), PackageUnit.fromCode(jpa.getPkgUnit()),
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm())));
        domain.assignSettlePartner(CustomerCode.of(jpa.getSettlePartnerCode()));
        if (jpa.getMasterBlId() != null) domain.linkToMaster(jpa.getMasterBlId());
        domain.updateTradeInfo(
                Incoterms.fromCode(jpa.getIncoterms()),
                SalesClass.fromCode(jpa.getSalesClass()),
                jpa.getMainItemName(),
                jpa.getHsCode());
    }

    private void copySeaFields(HouseBlSeaJpaEntity jpa, HouseBlSea domain) {
        domain.updateSeaSchedule(LinerCode.of(jpa.getLinerCode()),
                VesselVoyage.of(jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getOnboardDate()));
        domain.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                PortCode.of(jpa.getPorCode()), PortCode.of(jpa.getFinalDestCode()),
                BlDate.of(jpa.getIssueDate()), NoOfBl.fromNumber(jpa.getNoOfBl()), PortCode.of(jpa.getIssuePlace()),
                BlDate.of(jpa.getDoDate()), PortCode.of(jpa.getPayableAt()),
                jpa.isTriangle(), BlNumber.of(jpa.getMblNo()),
                jpa.getLoadType()));
    }

    private void copyAirFields(HouseBlAirJpaEntity jpa, HouseBlAir domain) {
        domain.updateAirFields(new HouseBlAir.AirFields(
                AirlineCode.of(jpa.getAirlineCode()),
                Weight.of(jpa.getChargeWeightKg()), Weight.of(jpa.getVolumeWeightKg()),
                RateClass.fromCode(jpa.getRateClass()), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(), FreightTerm.fromCode(jpa.getOtherTerm()),
                BlDate.of(jpa.getIssueDate()), PortCode.of(jpa.getIssuePlace()), jpa.getSignature(),
                Fhd.fromCode(jpa.getFhd()),
                HandlingInformation.of(
                        HandlingInfoCode.fromCode(jpa.getHandlingInfoCode()),
                        jpa.getHandlingInfoText()),
                jpa.getOriginOfGoods(),
                CargoType.fromCode(jpa.getCargoType())));
    }

    private void copyTruckFields(HouseBlTruckJpaEntity jpa, HouseBlTruck domain) {
        domain.updateTruckFields(new HouseBlTruck.TruckFields(
                VesselVoyage.of(jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getPickupDate()), jpa.getPickupTm(),
                jpa.getEtdTm(), jpa.getEtaTm(),
                jpa.getLoadType(), jpa.getServiceTerm(),
                CustomerCode.of(jpa.getTruckerCode()),
                EmployeeCode.of(jpa.getTruckerPic()),
                Weight.of(jpa.getChargeWeightKg())));
    }

    private void copyNonBlFields(HouseBlNonBlJpaEntity jpa, HouseBlNonBl domain) {
        domain.updateNonBlFields(BlNumber.of(jpa.getOriginalBlRef()),
                Rton.of(jpa.getRton()), Weight.of(jpa.getVolumeWtKg()));
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
        jpa.setShipperAddress(mapOrNull(domain.getShipperCode(), CustomerCode::address));
        jpa.setConsigneeCode(mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
        jpa.setConsigneeAddress(mapOrNull(domain.getConsigneeCode(), CustomerCode::address));
        jpa.setNotifyCode(mapOrNull(domain.getNotifyCode(), CustomerCode::value));
        jpa.setNotifyAddress(mapOrNull(domain.getNotifyCode(), CustomerCode::address));
        jpa.setDocPartnerCode(mapOrNull(domain.getDocPartnerCode(), CustomerCode::value));
        jpa.setDocPartnerAddress(mapOrNull(domain.getDocPartnerCode(), CustomerCode::address));
        jpa.setDeliveryCode(mapOrNull(domain.getDeliveryCode(), PortCode::value));
        jpa.setPkgQty(mapOrNull(domain.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(mapOrNull(domain.getPkgUnit(), PackageUnit::name));
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setIncoterms(mapOrNull(domain.getIncoterms(), Incoterms::name));
        jpa.setSalesClass(mapOrNull(domain.getSalesClass(), SalesClass::getCode));
        jpa.setMainItemName(domain.getMainItemName());
        jpa.setHsCode(domain.getHsCode());
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
        jpa.setPayableAt(mapOrNull(domain.getPayableAt(), PortCode::value));
        jpa.setIsTriangle(domain.isTriangle());
        jpa.setMblNo(mapOrNull(domain.getMblNo(), BlNumber::value));
    }

    public void applyAirFields(HouseBlAir domain, HouseBlAirJpaEntity jpa) {
        jpa.setAirlineCode(mapOrNull(domain.getAirlineCode(), AirlineCode::value));
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
        HandlingInformation hi = domain.getHandlingInformation();
        jpa.setHandlingInfoCode(hi == null ? null : mapOrNull(hi.code(), HandlingInfoCode::getCode));
        jpa.setHandlingInfoText(hi == null ? null : hi.description());
        jpa.setOriginOfGoods(domain.getOriginOfGoods());
        jpa.setCargoType(mapOrNull(domain.getCargoType(), CargoType::getCode));
    }

    public void applyTruckFields(HouseBlTruck domain, HouseBlTruckJpaEntity jpa) {
        VesselVoyage vv = domain.getVesselVoyage();
        jpa.setVesselName(vv != null ? vv.vesselName() : "TRUCK");
        jpa.setVoyageNo(vv != null ? vv.voyageNo() : null);
        jpa.setPickupDate(mapOrNull(domain.getPickupDate(), BlDate::asString));
        jpa.setPickupTm(domain.getPickupTm());
        jpa.setEtdTm(domain.getEtdTm());
        jpa.setEtaTm(domain.getEtaTm());
        jpa.setLoadType(domain.getLoadType());
        jpa.setServiceTerm(domain.getServiceTerm());
        jpa.setTruckerCode(mapOrNull(domain.getTruckerCode(), CustomerCode::value));
        jpa.setTruckerPic(mapOrNull(domain.getTruckerPic(), EmployeeCode::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
    }

    public void applyNonBlFields(HouseBlNonBl domain, HouseBlNonBlJpaEntity jpa) {
        jpa.setWorkDivision(domain.getWorkDivision());
        jpa.setOriginalBlRef(mapOrNull(domain.getOriginalBlRef(), BlNumber::value));
        jpa.setRton(mapOrNull(domain.getRton(), Rton::ton));
        jpa.setVolumeWtKg(mapOrNull(domain.getVolumeWtKg(), Weight::kg));
    }
}
