package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.common.util.Nullables;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HouseBlJpaToDomainMapper {

    private final HouseBlCargoMapper cargoMapper;
    private final HouseBlDocMapper docMapper;

    public HouseBlSea toSeaDomain(HouseBlJpaEntity jpa, HouseBlSeaJpaEntity seaJpa, HouseBlSeaDescJpaEntity descJpa) {
        HouseBlSea domain = HouseBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (seaJpa != null) copySeaFields(seaJpa, domain);
        // SEA 컨테이너는 seaJpa 소유 — @BatchSize(50)으로 batch fetch
        List<HouseBlContainer> containers = seaJpa != null
                ? seaJpa.getContainers().stream().map(c -> cargoMapper.toSeaContainerDomain(c, domain)).collect(Collectors.toList())
                : List.of();
        domain.initContainers(containers);
        if (descJpa != null) {
            domain.initDesc(docMapper.toSeaDescDomain(descJpa));
        }
        return domain;
    }

    public HouseBlAir toAirDomain(HouseBlJpaEntity jpa, HouseBlAirJpaEntity airJpa, HouseBlAirDescJpaEntity descJpa) {
        HouseBlAir domain = HouseBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (airJpa != null) copyAirFields(airJpa, domain);
        List<HouseBlDim> dims = airJpa != null
                ? airJpa.getDims().stream().map(cargoMapper::toAirDimDomain).collect(Collectors.toList())
                : List.of();
        domain.initDims(dims);
        // scheduleLegs/airCharges는 HouseBlAirJpaEntity 소유 (house_bl_air_id FK)
        List<HouseBlScheduleLeg> scheduleLegs = airJpa != null
                ? airJpa.getScheduleLegs().stream().map(docMapper::toScheduleLegDomain).collect(Collectors.toList())
                : List.of();
        domain.initScheduleLegs(scheduleLegs);
        List<HouseBlAirCharge> airCharges = airJpa != null
                ? airJpa.getAirCharges().stream().map(docMapper::toAirChargeDomain).collect(Collectors.toList())
                : List.of();
        domain.initAirCharges(airCharges);
        if (descJpa != null) {
            domain.initDesc(docMapper.toAirDescDomain(descJpa));
        }
        return domain;
    }

    public HouseBlTruck toTruckDomain(HouseBlJpaEntity jpa, HouseBlTruckJpaEntity truckJpa, HouseBlTruckDescJpaEntity descJpa) {
        HouseBlTruck domain = HouseBlTruck.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (truckJpa != null) copyTruckFields(truckJpa, domain);
        List<HouseBlDim> dims = truckJpa != null
                ? truckJpa.getDims().stream().map(cargoMapper::toTruckDimDomain).collect(Collectors.toList())
                : List.of();
        domain.initDims(dims);
        List<HouseBlTruckOrder> truckOrders = truckJpa != null
                ? truckJpa.getTruckOrders().stream().map(docMapper::toTruckOrderDomain).collect(Collectors.toList())
                : List.of();
        domain.initTruckOrders(truckOrders);
        if (descJpa != null) {
            domain.initDesc(docMapper.toTruckDescDomain(descJpa));
        }
        return domain;
    }

    public HouseBlNonBl toNonBlDomain(HouseBlJpaEntity jpa, HouseBlNonBlJpaEntity nonBlJpa) {
        HouseBlNonBl domain = HouseBlNonBl.create(Nullables.mapOrNull(nonBlJpa, HouseBlNonBlJpaEntity::getWorkDivision), jpa.getBound());
        copyBaseFields(jpa, domain);
        if (nonBlJpa != null) copyNonBlFields(nonBlJpa, domain);
        // NON_BL 컨테이너는 nonBlJpa 소유 — @BatchSize(50)으로 batch fetch
        List<HouseBlContainer> containers = nonBlJpa != null
                ? nonBlJpa.getContainers().stream().map(c -> cargoMapper.toNonBlContainerDomain(c, domain)).collect(Collectors.toList())
                : List.of();
        domain.initContainers(containers);
        List<HouseBlDim> dims = nonBlJpa != null
                ? nonBlJpa.getDims().stream().map(cargoMapper::toNonBlDimDomain).collect(Collectors.toList())
                : List.of();
        domain.initDims(dims);
        // NON_BL은 desc를 사용하지 않음 — house_bl_non_bl.remark 컬럼으로 이전됨
        if (nonBlJpa != null) domain.updateRemark(nonBlJpa.getRemark());
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
        domain.updateCargoSummary(new CargoSummary(Quantity.of(jpa.getPkgQty()),
                jpa.getPkgUnit(),
                jpa.getWeightUnit(),
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
                Rton.of(jpa.getRton()),
                jpa.getSayInformation(),
                jpa.getNoOfContainerOrPackages());
        domain.updateBlType(jpa.getBlType());
        domain.updateRemark(jpa.getRemark());
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
        domain.updateRemark(jpa.getRemark());
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
        domain.updateRemark(jpa.getRemark());
    }

    private void copyNonBlFields(HouseBlNonBlJpaEntity jpa, HouseBlNonBl domain) {
        domain.updateNonBlFields(BlNumber.of(jpa.getOriginalBlRef()),
                Rton.of(jpa.getRton()), Weight.of(jpa.getVolumeWtKg()));
        domain.updateScheduleFields(
                jpa.getLinerCode(), jpa.getLinerName(), jpa.getVesselName(), jpa.getVoyageNo(),
                jpa.getFinalDestCode(), jpa.getFinalDestName(), jpa.getFinalEta());
        domain.assignVolumeDivisor(jpa.getVolumeDivisor());
    }
}
