package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl.WorkDivision;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import org.springframework.stereotype.Component;

/**
 * Command → 도메인 Entity 변환 팩토리.
 * Adapter(in)에서 분리된 VO/Entity 생성 로직을 이 계층에 집중한다.
 */
@Component
public class HouseBlFactory {

    private final HouseBlSubFactory sub;

    public HouseBlFactory(HouseBlSubFactory sub) {
        this.sub = sub;
    }

    // ── CREATE ────────────────────────────────────────────────────────

    public HouseBl toEntity(CreateHouseBlCommand cmd) {
        JobDiv jobDiv = JobDiv.valueOf(cmd.jobDiv());
        Bound bound = Bound.valueOf(cmd.bound());

        HouseBl entity = switch (jobDiv) {
            case SEA    -> HouseBlSea.create(bound);
            case AIR    -> HouseBlAir.create(bound);
            case TRUCK  -> HouseBlTruck.create(bound);
            case NON_BL -> {
                WorkDivision wd = cmd.workDivision() != null
                        ? WorkDivision.valueOf(cmd.workDivision())
                        : WorkDivision.SEA;
                yield HouseBlNonBl.create(wd, bound);
            }
        };

        applyCommonCreate(entity, cmd);
        applySeaCreate(entity, cmd.seaDetail());
        applyNonBlCreate(entity, cmd);
        applySubCreate(entity, cmd);
        return entity;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    public void applyToEntity(UpdateHouseBlCommand cmd, HouseBl entity) {
        entity.update(toUpdateFields(cmd));
        entity.assignMasterReference(MblNo.of(cmd.mblNo()), cmd.masterRefNo());
        applySeaUpdate(entity, cmd.seaDetail());
        applyNonBlUpdate(entity, cmd);
        applySubUpdate(entity, cmd);
    }

    // ── 공통 필드 매핑 (CREATE) ───────────────────────────────────────

    private void applyCommonCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        if (cmd.hblNo() != null) entity.assignHblNo(BlNumber.of(cmd.hblNo()));
        entity.updateBlStatus(
                Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf),
                Nullables.mapOrNull(cmd.freightTerm(), FreightTerm::valueOf));
        entity.assignParties(
                CustomerCode.of(cmd.shipperCode()), CustomerCode.of(cmd.consigneeCode()),
                CustomerCode.of(cmd.notifyCode()), CustomerCode.of(cmd.docPartnerCode()),
                PortCode.of(Nullables.mapOrNull(cmd.seaDetail(), CreateHouseBlCommand.SeaDetailCommand::deliveryCode)));
        entity.updateSchedule(
                PortCode.of(cmd.polCode()), PortCode.of(cmd.podCode()),
                BlDate.of(cmd.etd()), BlDate.of(cmd.eta()));
        entity.updateCargoSummary(new CargoSummary(
                Quantity.of(cmd.pkgQty()),
                WeightUnit.fromCodeOrDefault(cmd.pkgUnit(), WeightUnit.KGS),
                Weight.of(cmd.grossWeightKg()), Volume.of(cmd.cbm())));
        entity.assignOperator(
                CustomerCode.of(cmd.actualCustomerCode()), EmployeeCode.of(cmd.operatorCode()),
                TeamCode.of(cmd.teamCode()), EmployeeCode.of(cmd.salesManCode()));
        if (cmd.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(cmd.settlePartnerCode()));
        entity.updateTradeInfo(
                Nullables.mapOrNull(cmd.incoterms(), Incoterms::fromCode),
                Nullables.mapOrNull(cmd.salesClass(), SalesClass::fromCode),
                cmd.mainItemName(), cmd.hsCode());
        if (cmd.masterBlId() != null) entity.linkToMaster(cmd.masterBlId());
        entity.assignMasterReference(MblNo.of(cmd.mblNo()), cmd.masterRefNo());
    }

    // ── 공통 필드 record 변환 (UPDATE) ───────────────────────────────

    private HouseBl.HouseBlUpdateFields toUpdateFields(UpdateHouseBlCommand cmd) {
        return new HouseBl.HouseBlUpdateFields(
                Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf),
                Nullables.mapOrNull(cmd.freightTerm(), FreightTerm::valueOf),
                Nullables.mapOrNull(cmd.shipperCode(), CustomerCode::of),
                Nullables.mapOrNull(cmd.consigneeCode(), CustomerCode::of),
                Nullables.mapOrNull(cmd.notifyCode(), CustomerCode::of),
                Nullables.mapOrNull(cmd.docPartnerCode(), CustomerCode::of),
                Nullables.mapOrNull(cmd.polCode(), PortCode::of),
                Nullables.mapOrNull(cmd.podCode(), PortCode::of),
                Nullables.mapOrNull(cmd.etd(), BlDate::of),
                Nullables.mapOrNull(cmd.eta(), BlDate::of),
                Nullables.mapOrNull(cmd.pkgQty(), Quantity::of),
                WeightUnit.fromCodeOrDefault(cmd.pkgUnit(), WeightUnit.KGS),
                Nullables.mapOrNull(cmd.grossWeightKg(), Weight::of),
                Nullables.mapOrNull(cmd.cbm(), Volume::of),
                Nullables.mapOrNull(cmd.actualCustomerCode(), CustomerCode::of),
                Nullables.mapOrNull(cmd.operatorCode(), EmployeeCode::of),
                Nullables.mapOrNull(cmd.teamCode(), TeamCode::of),
                Nullables.mapOrNull(cmd.salesManCode(), EmployeeCode::of),
                Nullables.mapOrNull(cmd.settlePartnerCode(), CustomerCode::of),
                Nullables.mapOrNull(cmd.incoterms(), Incoterms::fromCode),
                Nullables.mapOrNull(cmd.salesClass(), SalesClass::fromCode),
                cmd.mainItemName(),
                cmd.hsCode(),
                cmd.masterBlId(),
                Nullables.mapOrNull(cmd.bound(), Bound::valueOf)
        );
    }

    // ── SEA 확장 필드 매핑 ────────────────────────────────────────────

    private void applySeaCreate(HouseBl entity, CreateHouseBlCommand.SeaDetailCommand s) {
        if (s == null || !(entity instanceof HouseBlSea sea)) return;
        sea.updateSeaSchedule(LinerCode.of(s.linerCode()),
                VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo()), BlDate.of(s.onboardDate()));
        sea.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                PortCode.of(s.porCode()), PortCode.of(s.finalDestCode()),
                BlDate.of(s.issueDate()), NoOfBl.fromNumber(s.noOfBl()),
                PortCode.of(s.issuePlace()), BlDate.of(s.doDate()), PortCode.of(s.payableAt()),
                Boolean.TRUE.equals(s.triangle()), Nullables.mapOrNull(s.loadType(), LoadType::valueOf)));
        applySeaCargoTerms(sea, s.serviceTerm(), s.weightUnit(), s.rton(), s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    private void applySeaUpdate(HouseBl entity, UpdateHouseBlCommand.SeaDetailCommand s) {
        if (s == null || !(entity instanceof HouseBlSea sea)) return;
        sea.updateSeaSchedule(
                Nullables.mapOrElse(s.linerCode(),   LinerCode::of,                                              sea::getLinerCode),
                Nullables.mapOrElse(s.vesselName(),  v -> VesselVoyage.of(s.vesselCode(), v, s.voyageNo()),       sea::getVesselVoyage),
                Nullables.mapOrElse(s.onboardDate(), BlDate::of,                                                  sea::getOnboardDate));
        if (s.porCode() != null || s.finalDestCode() != null || s.issueDate() != null
                || s.noOfBl() != null || s.issuePlace() != null || s.doDate() != null
                || s.payableAt() != null || s.triangle() != null || s.loadType() != null) {
            sea.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                    Nullables.mapOrElse(s.porCode(),       PortCode::of,          sea::getPorCode),
                    Nullables.mapOrElse(s.finalDestCode(), PortCode::of,          sea::getFinalDestCode),
                    Nullables.mapOrElse(s.issueDate(),     BlDate::of,            sea::getIssueDate),
                    Nullables.mapOrElse(s.noOfBl(),        NoOfBl::fromNumber,    sea::getNoOfBl),
                    Nullables.mapOrElse(s.issuePlace(),    PortCode::of,          sea::getIssuePlace),
                    Nullables.mapOrElse(s.doDate(),        BlDate::of,            sea::getDoDate),
                    Nullables.mapOrElse(s.payableAt(),     PortCode::of,          sea::getPayableAt),
                    Nullables.firstNonNull(s.triangle(),                           sea::isTriangle),
                    Nullables.mapOrElse(s.loadType(),      LoadType::valueOf,     sea::getLoadType)));
        }
        applySeaCargoTerms(sea, s.serviceTerm(), s.weightUnit(), s.rton(), s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    private void applySeaCargoTerms(HouseBlSea sea, String serviceTerm, String weightUnit,
                                    java.math.BigDecimal rton, String sayInfo, String noOfCtnr) {
        if (serviceTerm != null || weightUnit != null || rton != null || sayInfo != null || noOfCtnr != null) {
            sea.updateSeaCargoTerms(
                    Nullables.mapOrElse(serviceTerm, ServiceTerm::fromLabel, sea::getServiceTerm),
                    Nullables.mapOrElse(weightUnit,  WeightUnit::fromCode,   sea::getWeightUnit),
                    Nullables.mapOrElse(rton,        Rton::of,               sea::getRton),
                    Nullables.firstNonNull(sayInfo,  sea::getSayInformation),
                    Nullables.firstNonNull(noOfCtnr, sea::getNoOfContainerOrPackages));
        }
    }

    // ── Non B/L 확장 필드 매핑 ────────────────────────────────────────

    private void applyNonBlCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        nonBl.updateNonBlFields(BlNumber.of(cmd.originalBlRef()), Rton.of(cmd.rton()), Weight.of(cmd.volumeWeightKg()));
        nonBl.updateScheduleFields(cmd.linerCode(), cmd.linerName(), cmd.vesselName(), cmd.voyageNo(),
                cmd.finalDestCode(), cmd.finalDestName(), cmd.finalEta());
        nonBl.assignVolumeDivisor(Nullables.mapOrNull(cmd.volumeDivisor(), VolumeDivisor::valueOf));
        nonBl.updateRemark(cmd.remark());
    }

    private void applyNonBlUpdate(HouseBl entity, UpdateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        if (cmd.workDivision() != null) nonBl.updateWorkDivision(WorkDivision.valueOf(cmd.workDivision()));
        nonBl.updateNonBlFields(BlNumber.of(cmd.originalBlRef()), Rton.of(cmd.rton()), Weight.of(cmd.volumeWeightKg()));
        nonBl.updateScheduleFields(cmd.linerCode(), cmd.linerName(), cmd.vesselName(), cmd.voyageNo(),
                cmd.finalDestCode(), cmd.finalDestName(), cmd.finalEta());
        nonBl.assignVolumeDivisor(Nullables.mapOrNull(cmd.volumeDivisor(), VolumeDivisor::valueOf));
        nonBl.updateRemark(cmd.remark());
    }

    // ── Sub 엔티티 일괄 적용 (CREATE) ────────────────────────────────

    private void applySubCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        sub.applyDesc(entity, cmd.desc());
        sub.applyDims(entity, cmd.dims());
        sub.applyContainers(entity, cmd.containers());
        sub.applyScheduleLegs(entity, cmd.scheduleLegs());
        sub.applyTruckOrders(entity, cmd.truckOrders());
        sub.applyAirCharges(entity, cmd.airCharges());
    }

    // ── Sub 엔티티 일괄 적용 (UPDATE) ────────────────────────────────

    private void applySubUpdate(HouseBl entity, UpdateHouseBlCommand cmd) {
        sub.applyDescUpdate(entity, cmd.desc());
        sub.applyDimsUpdate(entity, cmd.dims());
        sub.applyContainersUpdate(entity, cmd.containers());
        sub.applyScheduleLegsUpdate(entity, cmd.scheduleLegs());
        sub.applyTruckOrdersUpdate(entity, cmd.truckOrders());
        sub.applyAirChargesUpdate(entity, cmd.airCharges());
    }

    // ── SearchCommand → Domain Filter 변환 ───────────────────────────

    public HouseBlFilter toFilter(SearchHouseBlCommand cmd) {
        return HouseBlFilter.of(
                Nullables.mapOrNull(cmd.jobDiv(), JobDiv::valueOf),
                Nullables.mapOrNull(cmd.bound(), Bound::valueOf),
                cmd.hblNo(), cmd.mblNo(),
                cmd.shipperCode(), cmd.consigneeCode(),
                cmd.polCode(), cmd.podCode(),
                cmd.etdFrom(), cmd.etdTo(),
                cmd.vessel(), cmd.voyage(),
                cmd.linerCode(), cmd.operatorCode(),
                cmd.teamCode(), cmd.partyCode(), cmd.portCode()
        ).withKinds(
                Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf),
                Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf),
                Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf)
        );
    }

    // ── Entity → Projection 변환 ─────────────────────────────────────

    public HouseBlDetailResult toDetailResult(HouseBl entity) {
        HouseBlNonBl nonBl = entity instanceof HouseBlNonBl n ? n : null;
        BlType seaBlType = entity instanceof HouseBlSea sea ? sea.getBlType() : null;
        return new HouseBlDetailResult(
                entity.getId(),
                VoMapper.mapOrNull(entity.getHblNo(), BlNumber::value),
                Nullables.mapOrNull(entity.getJobDiv(), JobDiv::name),
                Nullables.mapOrNull(entity.getBound(), Bound::name),
                Nullables.mapOrNull(entity.getShipmentType(), ShipmentType::name),
                Nullables.mapOrNull(seaBlType, BlType::name),
                Nullables.mapOrNull(entity.getFreightTerm(), FreightTerm::name),
                VoMapper.mapOrNull(entity.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getDocPartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getPolCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getPodCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getDeliveryCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getEtd(), BlDate::asString),
                VoMapper.mapOrNull(entity.getEta(), BlDate::asString),
                VoMapper.mapOrNull(entity.getPkgQty(), Quantity::count),
                Nullables.mapOrNull(entity.getPkgUnit(), WeightUnit::name),
                VoMapper.mapOrNull(entity.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(entity.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(entity.getActualCustomerCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(entity.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(entity.getSalesManCode(), EmployeeCode::value),
                entity.getMasterBlId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                Nullables.mapOrNull(nonBl, n -> VoMapper.mapOrNull(n.getOriginalBlRef(), BlNumber::value)),
                Nullables.mapOrNull(nonBl, n -> Nullables.mapOrNull(n.getWorkDivision(), WorkDivision::name)),
                Nullables.mapOrNull(nonBl, HouseBlNonBl::getLinerCode),
                Nullables.mapOrNull(nonBl, HouseBlNonBl::getLinerName),
                Nullables.mapOrNull(nonBl, HouseBlNonBl::getVesselName),
                Nullables.mapOrNull(nonBl, HouseBlNonBl::getVoyageNo),
                Nullables.mapOrNull(nonBl, HouseBlNonBl::getFinalDestCode),
                Nullables.mapOrNull(nonBl, HouseBlNonBl::getFinalDestName),
                Nullables.mapOrNull(nonBl, HouseBlNonBl::getFinalEta),
                Nullables.mapOrNull(nonBl, n -> VoMapper.mapOrNull(n.getVolumeWtKg(), Weight::kg)),
                Nullables.mapOrNull(nonBl, n -> VoMapper.mapOrNull(n.getRton(), Rton::ton))
        );
    }
}
