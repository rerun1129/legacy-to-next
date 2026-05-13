package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.application.housebl.projection.SeaContainerProjection;
import com.freightos.fms.application.housebl.projection.SeaDescProjection;
import com.freightos.fms.application.housebl.projection.SeaDetailProjection;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl.WorkDivision;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Command → 도메인 Entity 변환 팩토리 (dispatcher).
 * 유형별 확장 필드 매핑은 각 SubFactory에 위임한다.
 */
@Component
public class HouseBlFactory {

    private final HouseBlSubFactory sub;
    private final HouseBlSeaSubFactory seaSubFactory;
    private final HouseBlTruckSubFactory truckSubFactory;
    private final HouseBlNonBlSubFactory nonBlSubFactory;
    private final HouseBlAirSubFactory airSubFactory;

    public HouseBlFactory(HouseBlSubFactory sub,
                          HouseBlSeaSubFactory seaSubFactory,
                          HouseBlTruckSubFactory truckSubFactory,
                          HouseBlNonBlSubFactory nonBlSubFactory,
                          HouseBlAirSubFactory airSubFactory) {
        this.sub = sub;
        this.seaSubFactory = seaSubFactory;
        this.truckSubFactory = truckSubFactory;
        this.nonBlSubFactory = nonBlSubFactory;
        this.airSubFactory = airSubFactory;
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
        seaSubFactory.applySeaCreate(entity, cmd.seaDetail());
        seaSubFactory.applySeaRemark(entity, cmd.remark());
        nonBlSubFactory.applyNonBlCreate(entity, cmd);
        truckSubFactory.applyTruckCreate(entity, cmd.truckDetail());
        truckSubFactory.applyTruckVolumeDivisor(entity, cmd.volumeDivisor());
        truckSubFactory.applyTruckRemark(entity, cmd.remark());
        airSubFactory.applyAirCreate(entity, cmd);
        applySubCreate(entity, cmd);
        return entity;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    public void applyToEntity(UpdateHouseBlCommand cmd, HouseBl entity) {
        entity.update(toUpdateFields(cmd));
        entity.assignMasterReference(MblNo.of(cmd.mblNo()), cmd.masterRefNo());
        seaSubFactory.applySeaUpdate(entity, cmd.seaDetail());
        seaSubFactory.applySeaRemark(entity, cmd.remark());
        nonBlSubFactory.applyNonBlUpdate(entity, cmd);
        truckSubFactory.applyTruckUpdate(entity, cmd.truckDetail());
        truckSubFactory.applyTruckVolumeDivisor(entity, cmd.volumeDivisor());
        truckSubFactory.applyTruckRemark(entity, cmd.remark());
        airSubFactory.applyAirUpdate(entity, cmd);
        applySubUpdate(entity, cmd);
    }

    // ── 공통 필드 매핑 (CREATE) ───────────────────────────────────────

    private void applyCommonCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        if (cmd.hblNo() != null) entity.assignHblNo(BlNumber.of(cmd.hblNo()));
        entity.updateBlStatus(
                Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf),
                Nullables.mapOrNull(cmd.freightTerm(), FreightTerm::valueOf));
        entity.assignParties(
                CustomerCode.of(cmd.shipperCode(), cmd.shipperAddress()),
                CustomerCode.of(cmd.consigneeCode(), cmd.consigneeAddress()),
                CustomerCode.of(cmd.notifyCode(), cmd.notifyAddress()),
                CustomerCode.of(cmd.docPartnerCode(), cmd.docPartnerAddress()),
                PortCode.of(Nullables.mapOrNull(cmd.seaDetail(), CreateHouseBlCommand.SeaDetailCommand::deliveryCode)));
        entity.updateSchedule(
                PortCode.of(cmd.polCode()), PortCode.of(cmd.podCode()),
                BlDate.of(cmd.etd()), BlDate.of(cmd.eta()));
        entity.updateCargoSummary(new CargoSummary(
                Quantity.of(cmd.pkgQty()),
                cmd.pkgUnit(),
                Nullables.mapOrNull(cmd.weightUnit(), WeightUnit::fromCode),
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
                Nullables.mapOrNull(cmd.shipperCode(), c -> CustomerCode.of(c, cmd.shipperAddress())),
                Nullables.mapOrNull(cmd.consigneeCode(), c -> CustomerCode.of(c, cmd.consigneeAddress())),
                Nullables.mapOrNull(cmd.notifyCode(), c -> CustomerCode.of(c, cmd.notifyAddress())),
                Nullables.mapOrNull(cmd.docPartnerCode(), c -> CustomerCode.of(c, cmd.docPartnerAddress())),
                Nullables.mapOrNull(cmd.polCode(), PortCode::of),
                Nullables.mapOrNull(cmd.podCode(), PortCode::of),
                Nullables.mapOrNull(cmd.etd(), BlDate::of),
                Nullables.mapOrNull(cmd.eta(), BlDate::of),
                Nullables.mapOrNull(cmd.pkgQty(), Quantity::of),
                cmd.pkgUnit(),
                Nullables.mapOrNull(cmd.weightUnit(), WeightUnit::fromCode),
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

    // ── SEA 자식 projection 변환 헬퍼 ────────────────────────────────

    private List<SeaContainerProjection> toSeaContainerProjections(List<HouseBlContainer> containers) {
        if (containers == null) return List.of();
        return containers.stream().map(c -> new SeaContainerProjection(
                c.getId(),
                VoMapper.mapOrNull(c.getContainerNo(), ContainerNumber::value),
                Nullables.mapOrNull(c.getContainerType(), ContainerType::name),
                c.getLengthFeet(),
                VoMapper.mapOrNull(c.getSealNo1(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo2(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo3(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo4(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo5(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo6(), SealNumber::value),
                VoMapper.mapOrNull(c.getPkgQty(), Quantity::count),
                c.getPkgUnit(),
                VoMapper.mapOrNull(c.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(c.getNetWeightKg(), Weight::kg),
                VoMapper.mapOrNull(c.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(c.getVgmKg(), Weight::kg),
                c.isSoc(),
                c.getSeq()
        )).toList();
    }

    private SeaDescProjection toSeaDescProjection(HouseBlDesc desc) {
        if (desc == null) return null;
        return new SeaDescProjection(
                desc.getMarks(),
                desc.getDescription(),
                Nullables.mapOrNull(desc.getDescClause1(), Enum::name),
                Nullables.mapOrNull(desc.getDescClause2(), Enum::name)
        );
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
        LoadType loadType = switch (entity) {
            case HouseBlSea sea -> sea.getLoadType();
            case HouseBlTruck truck -> truck.getLoadType();
            default -> null;
        };
        String remark = switch (entity) {
            case HouseBlSea sea -> sea.getRemark();
            case HouseBlAir air -> air.getRemark();
            case HouseBlTruck truck -> truck.getRemark();
            default -> null;
        };
        SeaDetailProjection seaDetail = (entity instanceof HouseBlSea sea) ? new SeaDetailProjection(
                VoMapper.mapOrNull(sea.getLinerCode(), LinerCode::value),
                Nullables.mapOrNull(sea.getVesselVoyage(), VesselVoyage::vesselCode),
                Nullables.mapOrNull(sea.getVesselVoyage(), VesselVoyage::vesselName),
                Nullables.mapOrNull(sea.getVesselVoyage(), VesselVoyage::voyageNo),
                VoMapper.mapOrNull(sea.getOnboardDate(), BlDate::asString),
                VoMapper.mapOrNull(sea.getPorCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getFinalDestCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getIssueDate(), BlDate::asString),
                Nullables.mapOrNull(sea.getNoOfBl(), NoOfBl::name),
                VoMapper.mapOrNull(sea.getIssuePlace(), PortCode::value),
                VoMapper.mapOrNull(sea.getDoDate(), BlDate::asString),
                VoMapper.mapOrNull(sea.getPayableAt(), PortCode::value),
                sea.isTriangle(),
                Nullables.mapOrNull(sea.getServiceTerm(), ServiceTerm::name),
                sea.getVesselNationality(),
                VoMapper.mapOrNull(sea.getRton(), Rton::ton),
                sea.getSayInformation(),
                sea.getNoOfContainerOrPackages(),
                toSeaContainerProjections(sea.getContainers()),
                toSeaDescProjection(sea.getDesc())
        ) : null;
        return new HouseBlDetailResult(
                entity.getId(),
                VoMapper.mapOrNull(entity.getHblNo(), BlNumber::value),
                Nullables.mapOrNull(entity.getJobDiv(), JobDiv::name),
                Nullables.mapOrNull(entity.getBound(), Bound::name),
                Nullables.mapOrNull(entity.getShipmentType(), ShipmentType::name),
                Nullables.mapOrNull(seaBlType, BlType::name),
                Nullables.mapOrNull(entity.getFreightTerm(), FreightTerm::name),
                VoMapper.mapOrNull(entity.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getShipperCode(), CustomerCode::address),
                VoMapper.mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getConsigneeCode(), CustomerCode::address),
                VoMapper.mapOrNull(entity.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getNotifyCode(), CustomerCode::address),
                VoMapper.mapOrNull(entity.getDocPartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getDocPartnerCode(), CustomerCode::address),
                VoMapper.mapOrNull(entity.getPolCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getPodCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getDeliveryCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getEtd(), BlDate::asString),
                VoMapper.mapOrNull(entity.getEta(), BlDate::asString),
                VoMapper.mapOrNull(entity.getPkgQty(), Quantity::count),
                entity.getPkgUnit(),
                Nullables.mapOrNull(entity.getWeightUnit(), WeightUnit::name),
                VoMapper.mapOrNull(entity.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(entity.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(entity.getActualCustomerCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(entity.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(entity.getSalesManCode(), EmployeeCode::value),
                entity.getMasterBlId(),
                VoMapper.mapOrNull(entity.getMblNo(), MblNo::value),
                VoMapper.mapOrNull(entity.getSettlePartnerCode(), CustomerCode::value),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                Nullables.mapOrNull(entity.getIncoterms(), Incoterms::name),
                Nullables.mapOrNull(entity.getSalesClass(), SalesClass::name),
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
                Nullables.mapOrNull(nonBl, n -> VoMapper.mapOrNull(n.getRton(), Rton::ton)),
                Nullables.mapOrNull(loadType, LoadType::name),
                remark,
                seaDetail
        );
    }
}
