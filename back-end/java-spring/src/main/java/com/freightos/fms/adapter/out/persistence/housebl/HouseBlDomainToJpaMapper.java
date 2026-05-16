package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setWeightUnit(domain.getWeightUnit());
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

    /**
     * Truck 전용 공통 필드 적용.
     * applyCommonFields와 달리 masterBlId는 setter를 두지 않아 DB 기존 값을 보호한다.
     * mblNo/masterRefNo는 도메인 값이 null이면 skip하여 DB 기존 값을 보호한다
     * (toolbar에서 편집한 경우 도메인에 값이 있으므로 dirty 발생 → UPDATE 반영).
     */
    public void applyTruckCommonFields(HouseBl domain, HouseBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setHouseBlId(domain.getId());
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(JobDiv.TRUCK);
        jpa.setHblNo(mapOrNull(domain.getHblNo(), BlNumber::value));
        jpa.setPolCode(mapOrNull(domain.getPolCode(), PortCode::value));
        jpa.setPodCode(mapOrNull(domain.getPodCode(), PortCode::value));
        jpa.setEtd(mapOrNull(domain.getEtd(), BlDate::asString));
        jpa.setEta(mapOrNull(domain.getEta(), BlDate::asString));
        jpa.setActualCustomerCode(mapOrNull(domain.getActualCustomerCode(), CustomerCode::value));
        jpa.setOperatorCode(mapOrNull(domain.getOperatorCode(), EmployeeCode::value));
        jpa.setTeamCode(mapOrNull(domain.getTeamCode(), TeamCode::value));
        jpa.setSalesManCode(mapOrNull(domain.getSalesManCode(), EmployeeCode::value));
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
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setWeightUnit(domain.getWeightUnit());
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setSalesClass(domain.getSalesClass());
        jpa.setMainItemName(domain.getMainItemName());
        jpa.setHsCode(domain.getHsCode());
        if (domain.getMblNo() != null) jpa.setMblNo(mapOrNull(domain.getMblNo(), MblNo::value));
        if (domain.getMasterRefNo() != null) jpa.setMasterRefNo(domain.getMasterRefNo());
    }

    /**
     * NonBl 전용 공통 필드 적용.
     * applyCommonFields와 거의 동일하나, NonBl form이 보내지 않는 필드는 SET하지 않아
     * DB 기존 값을 보호한다: shipperAddress, consigneeAddress, notifyAddress,
     * docPartnerAddress, incoterms, masterBlId.
     * mblNo/masterRefNo는 도메인 값이 null이면 skip하여 DB 기존 값을 보호한다
     * (toolbar에서 편집한 경우 도메인에 값이 있으므로 dirty 발생 → UPDATE 반영).
     */
    public void applyNonBlCommonFields(HouseBl domain, HouseBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setHouseBlId(domain.getId());
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(JobDiv.NON_BL);
        jpa.setHblNo(mapOrNull(domain.getHblNo(), BlNumber::value));
        jpa.setPolCode(mapOrNull(domain.getPolCode(), PortCode::value));
        jpa.setPodCode(mapOrNull(domain.getPodCode(), PortCode::value));
        jpa.setEtd(mapOrNull(domain.getEtd(), BlDate::asString));
        jpa.setEta(mapOrNull(domain.getEta(), BlDate::asString));
        jpa.setActualCustomerCode(mapOrNull(domain.getActualCustomerCode(), CustomerCode::value));
        jpa.setOperatorCode(mapOrNull(domain.getOperatorCode(), EmployeeCode::value));
        jpa.setTeamCode(mapOrNull(domain.getTeamCode(), TeamCode::value));
        jpa.setSalesManCode(mapOrNull(domain.getSalesManCode(), EmployeeCode::value));
        jpa.setShipmentType(domain.getShipmentType());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setShipperCode(mapOrNull(domain.getShipperCode(), CustomerCode::value));
        jpa.setConsigneeCode(mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
        jpa.setNotifyCode(mapOrNull(domain.getNotifyCode(), CustomerCode::value));
        jpa.setDocPartnerCode(mapOrNull(domain.getDocPartnerCode(), CustomerCode::value));
        jpa.setPkgQty(mapOrNull(domain.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setWeightUnit(domain.getWeightUnit());
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setSalesClass(domain.getSalesClass());
        jpa.setMainItemName(domain.getMainItemName());
        jpa.setHsCode(domain.getHsCode());
        if (domain.getMblNo() != null) jpa.setMblNo(mapOrNull(domain.getMblNo(), MblNo::value));
        if (domain.getMasterRefNo() != null) jpa.setMasterRefNo(domain.getMasterRefNo());
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
        jpa.setRton(mapOrNull(domain.getRton(), Rton::ton));
        jpa.setSayInformation(domain.getSayInformation());
        jpa.setNoOfContainerOrPackages(domain.getNoOfContainerOrPackages());
        jpa.setBlType(domain.getBlType());
        jpa.setDeliveryCode(mapOrNull(domain.getDeliveryCode(), PortCode::value));
        jpa.setRemark(domain.getRemark());
    }

    /**
     * Sea 전용 공통 필드 적용 (Update 경로 전용).
     * masterBlId는 setter를 두지 않아 DB 기존 값을 보호한다 (§6.37 sub-set — Truck·NonBl 선례 동일 패턴).
     * mblNo/masterRefNo는 도메인 값이 null이면 skip하여 DB 기존 값을 보호한다
     * (toolbar에서 편집한 경우 도메인에 값이 있으므로 dirty 발생 → UPDATE 반영).
     */
    public void applySeaCommonFields(HouseBl domain, HouseBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setHouseBlId(domain.getId());
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(JobDiv.SEA);
        jpa.setHblNo(mapOrNull(domain.getHblNo(), BlNumber::value));
        jpa.setPolCode(mapOrNull(domain.getPolCode(), PortCode::value));
        jpa.setPodCode(mapOrNull(domain.getPodCode(), PortCode::value));
        jpa.setEtd(mapOrNull(domain.getEtd(), BlDate::asString));
        jpa.setEta(mapOrNull(domain.getEta(), BlDate::asString));
        jpa.setActualCustomerCode(mapOrNull(domain.getActualCustomerCode(), CustomerCode::value));
        jpa.setOperatorCode(mapOrNull(domain.getOperatorCode(), EmployeeCode::value));
        jpa.setTeamCode(mapOrNull(domain.getTeamCode(), TeamCode::value));
        jpa.setSalesManCode(mapOrNull(domain.getSalesManCode(), EmployeeCode::value));
        jpa.setShipmentType(domain.getShipmentType());
        jpa.setFreightTerm(domain.getFreightTerm());
        jpa.setShipperCode(mapOrNull(domain.getShipperCode(), CustomerCode::value));
        // address 4종: trim-aware compare로 DB의 trailing whitespace 가짜 dirty 차단 (§6.63)
        setAddressIfChangedTrim(jpa::getShipperAddress,    jpa::setShipperAddress,    mapOrNull(domain.getShipperCode(), CustomerCode::address));
        jpa.setConsigneeCode(mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
        setAddressIfChangedTrim(jpa::getConsigneeAddress,  jpa::setConsigneeAddress,  mapOrNull(domain.getConsigneeCode(), CustomerCode::address));
        jpa.setNotifyCode(mapOrNull(domain.getNotifyCode(), CustomerCode::value));
        setAddressIfChangedTrim(jpa::getNotifyAddress,     jpa::setNotifyAddress,     mapOrNull(domain.getNotifyCode(), CustomerCode::address));
        jpa.setDocPartnerCode(mapOrNull(domain.getDocPartnerCode(), CustomerCode::value));
        setAddressIfChangedTrim(jpa::getDocPartnerAddress, jpa::setDocPartnerAddress, mapOrNull(domain.getDocPartnerCode(), CustomerCode::address));
        jpa.setPkgQty(mapOrNull(domain.getPkgQty(), Quantity::count));
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setWeightUnit(domain.getWeightUnit());
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setSalesClass(domain.getSalesClass());
        jpa.setMainItemName(domain.getMainItemName());
        jpa.setHsCode(domain.getHsCode());
        if (domain.getMblNo() != null) jpa.setMblNo(mapOrNull(domain.getMblNo(), MblNo::value));
        if (domain.getMasterRefNo() != null) jpa.setMasterRefNo(domain.getMasterRefNo());
    }

    /**
     * Sea 전용 ext 필드 적용 (Update 경로 전용).
     * applySeaFields와 동일 내용 — UPDATE 어댑터 전용 명명으로 책임을 분리한다 (§6.37).
     */
    public void applySeaBlFields(HouseBlSea domain, HouseBlSeaJpaEntity jpa) {
        applySeaFields(domain, jpa);
    }

    /**
     * AIR 전용 공통 필드 적용 (Update 경로 전용).
     * masterBlId는 setter를 두지 않아 DB 기존 값을 보호한다 (§6.37 sub-set — Sea/Truck/NonBl 선례 동일 패턴).
     * mblNo/masterRefNo는 도메인 값이 null이면 skip하여 DB 기존 값을 보호한다
     * (toolbar에서 편집한 경우 도메인에 값이 있으므로 dirty 발생 → UPDATE 반영).
     */
    public void applyAirCommonFields(HouseBl domain, HouseBlJpaEntity jpa) {
        if (domain.getId() != null) jpa.setHouseBlId(domain.getId());
        jpa.setBound(domain.getBound());
        jpa.setJobDiv(JobDiv.AIR);
        jpa.setHblNo(mapOrNull(domain.getHblNo(), BlNumber::value));
        jpa.setPolCode(mapOrNull(domain.getPolCode(), PortCode::value));
        jpa.setPodCode(mapOrNull(domain.getPodCode(), PortCode::value));
        jpa.setEtd(mapOrNull(domain.getEtd(), BlDate::asString));
        jpa.setEta(mapOrNull(domain.getEta(), BlDate::asString));
        jpa.setActualCustomerCode(mapOrNull(domain.getActualCustomerCode(), CustomerCode::value));
        jpa.setOperatorCode(mapOrNull(domain.getOperatorCode(), EmployeeCode::value));
        jpa.setTeamCode(mapOrNull(domain.getTeamCode(), TeamCode::value));
        jpa.setSalesManCode(mapOrNull(domain.getSalesManCode(), EmployeeCode::value));
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
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setWeightUnit(domain.getWeightUnit());
        jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setSalesClass(domain.getSalesClass());
        jpa.setMainItemName(domain.getMainItemName());
        jpa.setHsCode(domain.getHsCode());
        if (domain.getMblNo() != null) jpa.setMblNo(mapOrNull(domain.getMblNo(), MblNo::value));
        if (domain.getMasterRefNo() != null) jpa.setMasterRefNo(domain.getMasterRefNo());
    }

    /**
     * AIR 전용 ext 필드 적용 (Update 경로 전용).
     * applyAirFields와 동일 내용 — UPDATE 어댑터 전용 명명으로 책임을 분리한다 (§6.37).
     */
    public void applyAirBlFields(HouseBlAir domain, HouseBlAirJpaEntity jpa) {
        applyAirFields(domain, jpa);
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
        jpa.setRemark(domain.getRemark());
    }

    public void applyTruckFields(HouseBlTruck domain, HouseBlTruckJpaEntity jpa) {
        VesselVoyage vv = domain.getVesselVoyage();
        jpa.setVesselName(vv != null ? vv.vesselName() : "TRUCK");
        jpa.setVoyageNo(Nullables.mapOrNull(vv, VesselVoyage::voyageNo));
        jpa.setPickupDate(mapOrNull(domain.getPickupDate(), BlDate::asString));
        jpa.setPickupTm(domain.getPickupTm());
        jpa.setEtdTm(domain.getEtdTm());
        jpa.setEtaTm(domain.getEtaTm());
        jpa.setLoadType(domain.getLoadType());
        jpa.setServiceTerm(domain.getServiceTerm());
        jpa.setTruckerCode(mapOrNull(domain.getTruckerCode(), CustomerCode::value));
        jpa.setTruckerPic(mapOrNull(domain.getTruckerPic(), EmployeeCode::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setVolumeDivisor(domain.getVolumeDivisor());
        jpa.setRemark(domain.getRemark());
    }

    /**
     * Truck 전용 확장 필드 적용 (Update 경로 전용).
     * Truck form 미보유 필드(pickupTm)는 SET하지 않아 DB 기존 값을 보호한다.
     * vesselName/voyageNo는 form 미보유 필드이므로 vv null이면 setter skip —
     * DB 기존 값(NULL 또는 'TRUCK')을 보호하여 무수정 조회→저장 시 dirty 미발생 (§6.37 sub-set).
     */
    public void applyTruckBlFields(HouseBlTruck domain, HouseBlTruckJpaEntity jpa) {
        VesselVoyage vv = domain.getVesselVoyage();
        // form 미보유(vesselName/voyageNo) — vv null이면 setter skip.
        // DB 기존 값(NULL 또는 'TRUCK')을 보호하여 무수정 조회→저장 시 dirty 미발생 (§6.37 sub-set).
        if (vv != null) {
            jpa.setVesselName(vv.vesselName());
            jpa.setVoyageNo(vv.voyageNo());
        }
        jpa.setPickupDate(mapOrNull(domain.getPickupDate(), BlDate::asString));
        jpa.setEtdTm(domain.getEtdTm());
        jpa.setEtaTm(domain.getEtaTm());
        jpa.setLoadType(domain.getLoadType());
        jpa.setServiceTerm(domain.getServiceTerm());
        jpa.setTruckerCode(mapOrNull(domain.getTruckerCode(), CustomerCode::value));
        jpa.setTruckerPic(mapOrNull(domain.getTruckerPic(), EmployeeCode::value));
        jpa.setChargeWeightKg(mapOrNull(domain.getChargeWeightKg(), Weight::kg));
        jpa.setVolumeDivisor(domain.getVolumeDivisor());
        jpa.setRemark(domain.getRemark());
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
        jpa.setVolumeDivisor(domain.getVolumeDivisor());
        jpa.setRemark(domain.getRemark());
    }

    /**
     * Address 류 (free-text) 컬럼 전용 — trim-aware compare. DB의 trailing/leading whitespace가
     * 남아 있을 때 FE submit의 trim과 round-trip 차이로 발생하던 가짜 dirty를 차단 (§6.63).
     * blank/whitespace-only는 null과 동등 처리.
     */
    private static void setAddressIfChangedTrim(Supplier<String> getter, Consumer<String> setter, String newValue) {
        String current = normalizeTrim(getter.get());
        String next    = normalizeTrim(newValue);
        if (!Objects.equals(current, next)) setter.accept(newValue);
    }

    private static String normalizeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
