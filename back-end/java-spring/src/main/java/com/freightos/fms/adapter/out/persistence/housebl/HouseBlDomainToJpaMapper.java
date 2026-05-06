package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.stereotype.Component;

import static com.freightos.common.util.VoMapper.mapOrNull;

@Component
public class HouseBlDomainToJpaMapper {

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
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setShipperCode(mapOrNull(domain.getShipperCode(), CustomerCode::value));
        jpa.setShipperAddress(mapOrNull(domain.getShipperCode(), CustomerCode::address));
        jpa.setConsigneeCode(mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
        jpa.setConsigneeAddress(mapOrNull(domain.getConsigneeCode(), CustomerCode::address));
        jpa.setNotifyCode(mapOrNull(domain.getNotifyCode(), CustomerCode::value));
        jpa.setNotifyAddress(mapOrNull(domain.getNotifyCode(), CustomerCode::address));
        jpa.setDocPartnerCode(mapOrNull(domain.getDocPartnerCode(), CustomerCode::value));
        jpa.setDocPartnerAddress(mapOrNull(domain.getDocPartnerCode(), CustomerCode::address));
        jpa.setPkgQty(mapOrNull(domain.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(mapOrNull(domain.getPkgUnit(), WeightUnit::name));
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setSalesClass(domain.getSalesClass());
        jpa.setMainItemName(domain.getMainItemName());
        jpa.setHsCode(domain.getHsCode());
        jpa.setMblNo(mapOrNull(domain.getMblNo(), MblNo::value));
        jpa.setMasterRefNo(domain.getMasterRefNo());
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
        jpa.setNoOfBl(domain.getNoOfBl());
        jpa.setIssuePlace(mapOrNull(domain.getIssuePlace(), PortCode::value));
        jpa.setDoDate(mapOrNull(domain.getDoDate(), BlDate::asString));
        jpa.setPayableAt(mapOrNull(domain.getPayableAt(), PortCode::value));
        jpa.setIsTriangle(domain.isTriangle());
        jpa.setServiceTerm(domain.getServiceTerm());
        if (domain.getVesselVoyage() != null) {
            jpa.setVesselCode(domain.getVesselVoyage().vesselCode());
        }
        jpa.setVesselNationality(domain.getVesselNationality());
        jpa.setWeightUnit(domain.getWeightUnit());
        jpa.setRton(mapOrNull(domain.getRton(), Rton::ton));
        jpa.setSayInformation(domain.getSayInformation());
        jpa.setNoOfContainerOrPackages(domain.getNoOfContainerOrPackages());
        jpa.setBlType(domain.getBlType());
        jpa.setDeliveryCode(mapOrNull(domain.getDeliveryCode(), PortCode::value));
    }

    public void applyAirFields(HouseBlAir domain, HouseBlAirJpaEntity jpa) {
        jpa.setAirlineCode(mapOrNull(domain.getAirlineCode(), AirlineCode::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setVolumeWeightKg(mapOrNull(domain.getVolumeWeightKg(), Weight::kg));
        jpa.setRateClass(domain.getRateClass());
        jpa.setCurrencyCode(mapOrNull(domain.getCurrencyCode(), CurrencyCode::value));
        jpa.setDeclaredValueCarriage(domain.getDeclaredValueCarriage());
        jpa.setDeclaredValueCustoms(domain.getDeclaredValueCustoms());
        jpa.setInsurance(domain.getInsurance());
        jpa.setAccountInformation(domain.getAccountInformation());
        jpa.setOtherTerm(domain.getOtherTerm());
        jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
        jpa.setIssuePlace(mapOrNull(domain.getIssuePlace(), PortCode::value));
        jpa.setSignature(domain.getSignature());
        jpa.setFhd(domain.getFhd());
        HandlingInformation hi = domain.getHandlingInformation();
        jpa.setHandlingInfoCode(hi == null ? null : hi.code());
        jpa.setHandlingInfoText(hi == null ? null : hi.description());
        jpa.setOriginOfGoods(domain.getOriginOfGoods());
        jpa.setCargoType(domain.getCargoType());
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
        jpa.setLinerCode(domain.getLinerCode());
        jpa.setLinerName(domain.getLinerName());
        jpa.setVesselName(domain.getVesselName());
        jpa.setVoyageNo(domain.getVoyageNo());
        jpa.setFinalDestCode(domain.getFinalDestCode());
        jpa.setFinalDestName(domain.getFinalDestName());
        jpa.setFinalEta(domain.getFinalEta());
    }
}
