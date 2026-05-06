package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HouseBlJpaToDomainMapper {

    private final HouseBlCargoMapper cargoMapper;
    private final HouseBlDocMapper docMapper;

    public HouseBlSea toSeaDomain(HouseBlJpaEntity jpa, HouseBlSeaJpaEntity seaJpa) {
        HouseBlSea domain = HouseBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (seaJpa != null) copySeaFields(seaJpa, domain);
        // @BatchSize(50)으로 리스트 조회 시 batch fetch
        List<HouseBlContainer> containers = jpa.getContainers().stream()
                .map(c -> toContainerDomain(c, domain))
                .collect(Collectors.toList());
        domain.initContainers(containers);
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
        domain.updateBlStatus(jpa.getShipmentType(), jpa.getFreightTerm());
        domain.assignParties(
                CustomerCode.of(jpa.getShipperCode(), jpa.getShipperAddress()),
                CustomerCode.of(jpa.getConsigneeCode(), jpa.getConsigneeAddress()),
                CustomerCode.of(jpa.getNotifyCode(), jpa.getNotifyAddress()),
                CustomerCode.of(jpa.getDocPartnerCode(), jpa.getDocPartnerAddress()),
                null);
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()), null, // weightUnit: sea/air 확장 테이블에서 별도 로드
                Weight.of(jpa.getGrossWeightKg()), Volume.of(jpa.getCbm())));
        domain.assignSettlePartner(CustomerCode.of(jpa.getSettlePartnerCode()));
        if (jpa.getMasterBlId() != null) domain.linkToMaster(jpa.getMasterBlId());
        domain.updateTradeInfo(
                jpa.getIncoterms(),
                jpa.getSalesClass(),
                jpa.getMainItemName(),
                jpa.getHsCode());
        domain.assignMasterReference(MblNo.of(jpa.getMblNo()), jpa.getMasterRefNo());
    }

    private void copySeaFields(HouseBlSeaJpaEntity jpa, HouseBlSea domain) {
        domain.updateSeaSchedule(LinerCode.of(jpa.getLinerCode()),
                VesselVoyage.of(jpa.getVesselCode(), jpa.getVesselName(), jpa.getVoyageNo()),
                BlDate.of(jpa.getOnboardDate()));
        domain.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                PortCode.of(jpa.getPorCode()), PortCode.of(jpa.getFinalDestCode()),
                BlDate.of(jpa.getIssueDate()), jpa.getNoOfBl(), PortCode.of(jpa.getIssuePlace()),
                BlDate.of(jpa.getDoDate()), PortCode.of(jpa.getPayableAt()),
                jpa.isTriangle(),
                jpa.getLoadType()));
        domain.assignDeliveryCode(PortCode.of(jpa.getDeliveryCode()));
        domain.updateVesselNationality(jpa.getVesselNationality());
        domain.updateSeaCargoTerms(
                jpa.getServiceTerm(),
                jpa.getWeightUnit(),
                Rton.of(jpa.getRton()),
                jpa.getSayInformation(),
                jpa.getNoOfContainerOrPackages());
        domain.updateBlType(jpa.getBlType());
    }

    private void copyAirFields(HouseBlAirJpaEntity jpa, HouseBlAir domain) {
        domain.updateAirFields(new HouseBlAir.AirFields(
                AirlineCode.of(jpa.getAirlineCode()),
                Weight.of(jpa.getChargeWeightKg()), Weight.of(jpa.getVolumeWeightKg()),
                jpa.getRateClass(), CurrencyCode.of(jpa.getCurrencyCode()),
                jpa.getDeclaredValueCarriage(), jpa.getDeclaredValueCustoms(),
                jpa.getInsurance(), jpa.getAccountInformation(), jpa.getOtherTerm(),
                BlDate.of(jpa.getIssueDate()), PortCode.of(jpa.getIssuePlace()), jpa.getSignature(),
                jpa.getFhd(),
                HandlingInformation.of(jpa.getHandlingInfoCode(), jpa.getHandlingInfoText()),
                jpa.getOriginOfGoods(),
                jpa.getCargoType()));
    }

    private void copyTruckFields(HouseBlTruckJpaEntity jpa, HouseBlTruck domain) {
        domain.updateTruckFields(new HouseBlTruck.TruckFields(
                VesselVoyage.of(null, jpa.getVesselName(), jpa.getVoyageNo()),
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
        domain.updateScheduleFields(
                jpa.getLinerCode(), jpa.getLinerName(), jpa.getVesselName(), jpa.getVoyageNo(),
                jpa.getFinalDestCode(), jpa.getFinalDestName(), jpa.getFinalEta());
    }

    private HouseBlContainer toContainerDomain(HouseBlContainerJpaEntity jpa, HouseBl parent) {
        HouseBlContainer c = HouseBlContainer.of(parent, ContainerNumber.of(jpa.getContainerNo()),
                jpa.getContainerType(), jpa.getLengthFeet());
        c.assignIdentity(jpa.getHouseBlContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        c.updateDetails(new HouseBlContainer.Details(
                SealNumber.of(jpa.getSealNo1()), SealNumber.of(jpa.getSealNo2()),
                SealNumber.of(jpa.getSealNo3()), SealNumber.of(jpa.getSealNo4()),
                SealNumber.of(jpa.getSealNo5()), SealNumber.of(jpa.getSealNo6()),
                Quantity.of(jpa.getPkgQty()), null, // weightUnit: container엔 별도 저장 없음
                Weight.of(jpa.getGrossWeightKg()), Weight.of(jpa.getNetWeightKg()),
                Volume.of(jpa.getCbm()), Weight.of(jpa.getVgmKg()), jpa.isSoc(), jpa.getSeq()));
        return c;
    }
}
