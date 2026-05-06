package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.common.util.VoMapper;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl.WorkDivision;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import org.springframework.stereotype.Component;

import java.util.List;

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
        applySeaUpdate(entity, cmd.seaDetail());
        applyNonBlUpdate(entity, cmd);
        applySubUpdate(entity, cmd);
    }

    // ── 공통 필드 매핑 (CREATE) ───────────────────────────────────────

    private void applyCommonCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        if (cmd.hblNo() != null) entity.assignHblNo(BlNumber.of(cmd.hblNo()));
        entity.updateBlStatus(
                cmd.shipmentType() != null ? ShipmentType.valueOf(cmd.shipmentType()) : null,
                cmd.freightTerm() != null ? FreightTerm.valueOf(cmd.freightTerm()) : null);
        entity.assignParties(
                CustomerCode.of(cmd.shipperCode()), CustomerCode.of(cmd.consigneeCode()),
                CustomerCode.of(cmd.notifyCode()), CustomerCode.of(cmd.docPartnerCode()),
                PortCode.of(cmd.seaDetail() != null ? cmd.seaDetail().deliveryCode() : null));
        entity.updateSchedule(
                PortCode.of(cmd.polCode()), PortCode.of(cmd.podCode()),
                BlDate.of(cmd.etd()), BlDate.of(cmd.eta()));
        entity.updateCargoSummary(new CargoSummary(
                Quantity.of(cmd.pkgQty()),
                cmd.pkgUnit() != null ? WeightUnit.fromCode(cmd.pkgUnit()) : null,
                Weight.of(cmd.grossWeightKg()), Volume.of(cmd.cbm())));
        entity.assignOperator(
                CustomerCode.of(cmd.actualCustomerCode()), EmployeeCode.of(cmd.operatorCode()),
                TeamCode.of(cmd.teamCode()), EmployeeCode.of(cmd.salesManCode()));
        if (cmd.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(cmd.settlePartnerCode()));
        entity.updateTradeInfo(
                cmd.incoterms() != null ? Incoterms.fromCode(cmd.incoterms()) : null,
                cmd.salesClass() != null ? SalesClass.fromCode(cmd.salesClass()) : null,
                cmd.mainItemName(), cmd.hsCode());
        if (cmd.masterBlId() != null) entity.linkToMaster(cmd.masterBlId());
    }

    // ── 공통 필드 record 변환 (UPDATE) ───────────────────────────────

    private HouseBl.HouseBlUpdateFields toUpdateFields(UpdateHouseBlCommand cmd) {
        String deliveryCode = cmd.seaDetail() != null ? cmd.seaDetail().deliveryCode() : null;
        return new HouseBl.HouseBlUpdateFields(
                cmd.hblNo()              != null ? BlNumber.of(cmd.hblNo())                   : null,
                cmd.shipmentType()       != null ? ShipmentType.valueOf(cmd.shipmentType()) : null,
                cmd.freightTerm()        != null ? FreightTerm.valueOf(cmd.freightTerm())      : null,
                cmd.shipperCode()        != null ? CustomerCode.of(cmd.shipperCode())          : null,
                cmd.consigneeCode()      != null ? CustomerCode.of(cmd.consigneeCode())        : null,
                cmd.notifyCode()         != null ? CustomerCode.of(cmd.notifyCode())           : null,
                cmd.docPartnerCode()     != null ? CustomerCode.of(cmd.docPartnerCode())       : null,
                cmd.polCode()            != null ? PortCode.of(cmd.polCode())                  : null,
                cmd.podCode()            != null ? PortCode.of(cmd.podCode())                  : null,
                cmd.etd()                != null ? BlDate.of(cmd.etd())                        : null,
                cmd.eta()                != null ? BlDate.of(cmd.eta())                        : null,
                cmd.pkgQty()             != null ? Quantity.of(cmd.pkgQty())                   : null,
                cmd.pkgUnit()            != null ? WeightUnit.fromCode(cmd.pkgUnit())          : null,
                cmd.grossWeightKg()      != null ? Weight.of(cmd.grossWeightKg())              : null,
                cmd.cbm()                != null ? Volume.of(cmd.cbm())                        : null,
                cmd.actualCustomerCode() != null ? CustomerCode.of(cmd.actualCustomerCode())   : null,
                cmd.operatorCode()       != null ? EmployeeCode.of(cmd.operatorCode())         : null,
                cmd.teamCode()           != null ? TeamCode.of(cmd.teamCode())                 : null,
                cmd.salesManCode()       != null ? EmployeeCode.of(cmd.salesManCode())         : null,
                cmd.settlePartnerCode()  != null ? CustomerCode.of(cmd.settlePartnerCode())    : null,
                cmd.incoterms()          != null ? Incoterms.fromCode(cmd.incoterms())         : null,
                cmd.salesClass()         != null ? SalesClass.fromCode(cmd.salesClass())       : null,
                cmd.mainItemName(),
                cmd.hsCode(),
                cmd.masterBlId()
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
                Boolean.TRUE.equals(s.triangle()), s.loadType() != null ? LoadType.valueOf(s.loadType()) : null));
        applySeaCargoTerms(sea, s.serviceTerm(), s.weightUnit(), s.rton(), s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    private void applySeaUpdate(HouseBl entity, UpdateHouseBlCommand.SeaDetailCommand s) {
        if (s == null || !(entity instanceof HouseBlSea sea)) return;
        sea.updateSeaSchedule(
                s.linerCode()   != null ? LinerCode.of(s.linerCode())                                   : sea.getLinerCode(),
                s.vesselName()  != null ? VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo()) : sea.getVesselVoyage(),
                s.onboardDate() != null ? BlDate.of(s.onboardDate())                                    : sea.getOnboardDate());
        if (s.porCode() != null || s.finalDestCode() != null || s.issueDate() != null
                || s.noOfBl() != null || s.issuePlace() != null || s.doDate() != null
                || s.payableAt() != null || s.triangle() != null || s.loadType() != null) {
            sea.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                    s.porCode()       != null ? PortCode.of(s.porCode())       : sea.getPorCode(),
                    s.finalDestCode() != null ? PortCode.of(s.finalDestCode()) : sea.getFinalDestCode(),
                    s.issueDate()     != null ? BlDate.of(s.issueDate())       : sea.getIssueDate(),
                    s.noOfBl()        != null ? NoOfBl.fromNumber(s.noOfBl())  : sea.getNoOfBl(),
                    s.issuePlace()    != null ? PortCode.of(s.issuePlace())    : sea.getIssuePlace(),
                    s.doDate()        != null ? BlDate.of(s.doDate())          : sea.getDoDate(),
                    s.payableAt()     != null ? PortCode.of(s.payableAt())     : sea.getPayableAt(),
                    s.triangle()      != null ? s.triangle()                   : sea.isTriangle(),
                    s.loadType()      != null ? LoadType.valueOf(s.loadType()) : sea.getLoadType()));
        }
        applySeaCargoTerms(sea, s.serviceTerm(), s.weightUnit(), s.rton(), s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    private void applySeaCargoTerms(HouseBlSea sea, String serviceTerm, String weightUnit,
                                    java.math.BigDecimal rton, String sayInfo, String noOfCtnr) {
        if (serviceTerm != null || weightUnit != null || rton != null || sayInfo != null || noOfCtnr != null) {
            sea.updateSeaCargoTerms(
                    serviceTerm != null ? ServiceTerm.fromLabel(serviceTerm) : sea.getServiceTerm(),
                    weightUnit  != null ? WeightUnit.fromCode(weightUnit)   : sea.getWeightUnit(),
                    rton        != null ? Rton.of(rton)                     : sea.getRton(),
                    sayInfo     != null ? sayInfo                           : sea.getSayInformation(),
                    noOfCtnr    != null ? noOfCtnr                         : sea.getNoOfContainerOrPackages());
        }
    }

    // ── Non B/L 확장 필드 매핑 ────────────────────────────────────────

    private void applyNonBlCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        nonBl.updateNonBlFields(BlNumber.of(cmd.originalBlRef()), Rton.of(cmd.rton()), Weight.of(cmd.volumeWeightKg()));
        nonBl.updateScheduleFields(cmd.linerCode(), cmd.linerName(), cmd.vesselName(), cmd.voyageNo(),
                cmd.finalDestCode(), cmd.finalDestName(), cmd.finalEta());
    }

    private void applyNonBlUpdate(HouseBl entity, UpdateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        nonBl.updateNonBlFields(BlNumber.of(cmd.originalBlRef()), Rton.of(cmd.rton()), Weight.of(cmd.volumeWeightKg()));
        nonBl.updateScheduleFields(cmd.linerCode(), cmd.linerName(), cmd.vesselName(), cmd.voyageNo(),
                cmd.finalDestCode(), cmd.finalDestName(), cmd.finalEta());
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

    // ── Entity → Projection 변환 ─────────────────────────────────────

    public HouseBlDetailResult toDetailResult(HouseBl entity) {
        HouseBlNonBl nonBl = entity instanceof HouseBlNonBl n ? n : null;
        return new HouseBlDetailResult(
                entity.getId(),
                VoMapper.mapOrNull(entity.getHblNo(), BlNumber::value),
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipmentType(),
                entity instanceof HouseBlSea sea ? sea.getBlType() : null,
                entity.getFreightTerm(),
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
                entity.getPkgUnit(),
                VoMapper.mapOrNull(entity.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(entity.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(entity.getActualCustomerCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(entity.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(entity.getSalesManCode(), EmployeeCode::value),
                entity.getMasterBlId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                nonBl != null ? VoMapper.mapOrNull(nonBl.getOriginalBlRef(), BlNumber::value) : null,
                nonBl != null && nonBl.getWorkDivision() != null ? nonBl.getWorkDivision().name() : null,
                nonBl != null ? nonBl.getLinerCode() : null,
                nonBl != null ? nonBl.getLinerName() : null,
                nonBl != null ? nonBl.getVesselName() : null,
                nonBl != null ? nonBl.getVoyageNo() : null,
                nonBl != null ? nonBl.getFinalDestCode() : null,
                nonBl != null ? nonBl.getFinalDestName() : null,
                nonBl != null ? nonBl.getFinalEta() : null,
                nonBl != null ? VoMapper.mapOrNull(nonBl.getVolumeWtKg(), Weight::kg) : null,
                nonBl != null ? VoMapper.mapOrNull(nonBl.getRton(), Rton::ton) : null
        );
    }
}
