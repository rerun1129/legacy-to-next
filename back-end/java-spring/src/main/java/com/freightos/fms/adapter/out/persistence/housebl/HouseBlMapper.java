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
import java.util.Optional;
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

    public HouseBlSea toSeaDomain(HouseBlJpaEntity jpa, HouseBlSeaJpaEntity seaJpa) {
        HouseBlSea domain = HouseBlSea.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (seaJpa != null) copySeaFields(seaJpa, domain);
        // @BatchSize(50)으로 리스트 조회 시 batch fetch
        List<HouseBlContainer> containers = jpa.getContainers().stream()
                .map(c -> toContainerDomain(c, domain))
                .collect(Collectors.toList());
        domain.initContainers(containers);
        return domain;
    }

    public HouseBlAir toAirDomain(HouseBlJpaEntity jpa, HouseBlAirJpaEntity airJpa) {
        HouseBlAir domain = HouseBlAir.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (airJpa != null) copyAirFields(airJpa, domain);
        return domain;
    }

    public HouseBlTruck toTruckDomain(HouseBlJpaEntity jpa, HouseBlTruckJpaEntity truckJpa) {
        HouseBlTruck domain = HouseBlTruck.create(jpa.getBound());
        copyBaseFields(jpa, domain);
        if (truckJpa != null) copyTruckFields(truckJpa, domain);
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

    // ── E-12 DIM ──────────────────────────────────────────────────────

    public HouseBlDim toDimDomain(HouseBlDimJpaEntity jpa) {
        HouseBlDim domain = HouseBlDim.create(
                jpa.getHouseBl().getHouseBlId(),
                jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm(),
                jpa.getQuantity(), jpa.getCbm(), jpa.getVolumeWeightKg(),
                jpa.getSeq());
        domain.assignIdentity(jpa.getHouseBlDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public List<HouseBlDim> toDimDomainList(List<HouseBlDimJpaEntity> jpaList) {
        return jpaList.stream().map(this::toDimDomain).collect(Collectors.toList());
    }

    public void applyDimFields(HouseBlDim domain, HouseBlDimJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setHouseBl(houseBlJpa);
        jpa.setLengthCm(domain.getLengthCm());
        jpa.setWidthCm(domain.getWidthCm());
        jpa.setHeightCm(domain.getHeightCm());
        jpa.setQuantity(domain.getQuantity());
        jpa.setCbm(domain.getCbm());
        jpa.setVolumeWeightKg(domain.getVolumeWeightKg());
        jpa.setSeq(domain.getSeq());
    }

    // ── E-13 DESC ─────────────────────────────────────────────────────

    public HouseBlDesc toDescDomain(HouseBlDescJpaEntity jpa) {
        HouseBlDesc domain = HouseBlDesc.create(jpa.getHouseBl().getHouseBlId());
        domain.assignIdentity(jpa.getHouseBlDescId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateContent(jpa.getMarksLeft(), jpa.getMarksRight(),
                jpa.getDescriptionLeft(), jpa.getDescriptionRight(),
                jpa.getDescClause1(), jpa.getDescClause2(), jpa.getRemark());
        return domain;
    }

    public Optional<HouseBlDesc> toDescDomain(Optional<HouseBlDescJpaEntity> jpa) {
        return jpa.map(this::toDescDomain);
    }

    public void applyDescFields(HouseBlDesc domain, HouseBlDescJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setHouseBl(houseBlJpa);
        jpa.setMarksLeft(domain.getMarksLeft());
        jpa.setMarksRight(domain.getMarksRight());
        jpa.setDescriptionLeft(domain.getDescriptionLeft());
        jpa.setDescriptionRight(domain.getDescriptionRight());
        jpa.setDescClause1(domain.getDescClause1());
        jpa.setDescClause2(domain.getDescClause2());
        jpa.setRemark(domain.getRemark());
    }

    // ── E-17 LICENSE ──────────────────────────────────────────────────

    public HouseBlLicense toLicenseDomain(HouseBlLicenseJpaEntity jpa) {
        HouseBlLicense domain = HouseBlLicense.create(
                jpa.getHouseBl().getHouseBlId(), jpa.getSeq());
        domain.assignIdentity(jpa.getHouseBlLicenseId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateDetails(jpa.getLicenseNo(), jpa.getPkgQty(), jpa.getPkgUnit(),
                jpa.getGrossWeightKg(), jpa.getCombinedPackingMark(),
                jpa.getCombinedPackingQty(), jpa.getCombinedPackingUnit(),
                jpa.isPartialShipment(), jpa.getPartialShipmentSeq(), jpa.getHsnNo());
        return domain;
    }

    public List<HouseBlLicense> toLicenseDomainList(List<HouseBlLicenseJpaEntity> jpaList) {
        return jpaList.stream().map(this::toLicenseDomain).collect(Collectors.toList());
    }

    public void applyLicenseFields(HouseBlLicense domain, HouseBlLicenseJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setHouseBl(houseBlJpa);
        jpa.setLicenseNo(domain.getLicenseNo());
        jpa.setPkgQty(domain.getPkgQty());
        jpa.setPkgUnit(domain.getPkgUnit());
        jpa.setGrossWeightKg(domain.getGrossWeightKg());
        jpa.setCombinedPackingMark(domain.getCombinedPackingMark());
        jpa.setCombinedPackingQty(domain.getCombinedPackingQty());
        jpa.setCombinedPackingUnit(domain.getCombinedPackingUnit());
        jpa.setPartialShipment(domain.isPartialShipment());
        jpa.setPartialShipmentSeq(domain.getPartialShipmentSeq());
        jpa.setHsnNo(domain.getHsnNo());
        jpa.setSeq(domain.getSeq());
    }

    // ── E-18 REFERENCE ────────────────────────────────────────────────

    public HouseBlReference toReferenceDomain(HouseBlReferenceJpaEntity jpa) {
        HouseBlReference domain = HouseBlReference.create(
                jpa.getHouseBl().getHouseBlId(),
                jpa.getReferenceType(), jpa.getReferenceNo(), jpa.getSeq());
        domain.assignIdentity(jpa.getHouseBlReferenceId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        return domain;
    }

    public List<HouseBlReference> toReferenceDomainList(List<HouseBlReferenceJpaEntity> jpaList) {
        return jpaList.stream().map(this::toReferenceDomain).collect(Collectors.toList());
    }

    public void applyReferenceFields(HouseBlReference domain, HouseBlReferenceJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setHouseBl(houseBlJpa);
        jpa.setReferenceType(domain.getReferenceType());
        jpa.setReferenceNo(domain.getReferenceNo());
        jpa.setSeq(domain.getSeq());
    }

    // ── E-19 SCHEDULE LEG ─────────────────────────────────────────────

    public HouseBlScheduleLeg toScheduleLegDomain(HouseBlScheduleLegJpaEntity jpa) {
        HouseBlScheduleLeg domain = HouseBlScheduleLeg.create(
                jpa.getHouseBl().getHouseBlId(),
                jpa.getToCode(), jpa.getOnBoardDt(), jpa.getArrivalDt(), jpa.getSeq());
        domain.assignIdentity(jpa.getHouseBlScheduleLegId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.updateDetails(jpa.getToCode(), jpa.getByCarrier(), jpa.getFlightNo(),
                jpa.getOnBoardDt(), jpa.getOnBoardTm(), jpa.getArrivalDt(), jpa.getArrivalTm());
        return domain;
    }

    public List<HouseBlScheduleLeg> toScheduleLegDomainList(List<HouseBlScheduleLegJpaEntity> jpaList) {
        return jpaList.stream().map(this::toScheduleLegDomain).collect(Collectors.toList());
    }

    public void applyScheduleLegFields(HouseBlScheduleLeg domain, HouseBlScheduleLegJpaEntity jpa, HouseBlJpaEntity houseBlJpa) {
        jpa.setHouseBl(houseBlJpa);
        jpa.setToCode(domain.getToCode());
        jpa.setByCarrier(domain.getByCarrier());
        jpa.setFlightNo(domain.getFlightNo());
        jpa.setOnBoardDt(domain.getOnBoardDt());
        jpa.setOnBoardTm(domain.getOnBoardTm());
        jpa.setArrivalDt(domain.getArrivalDt());
        jpa.setArrivalTm(domain.getArrivalTm());
        jpa.setSeq(domain.getSeq());
    }
}
