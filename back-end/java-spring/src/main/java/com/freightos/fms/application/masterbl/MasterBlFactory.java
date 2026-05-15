package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.ConsoledHouseBlSummaryView;
import com.freightos.fms.application.masterbl.projection.ConsoledSeaContainerView;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.SeaDescProjection;
import com.freightos.fms.application.masterbl.projection.SeaDetailProjection;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CargoSummary;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.LinerCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.TeamCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledSeaContainer;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Command → 도메인 Entity 변환 팩토리 (dispatcher).
 * Sea 확장 필드 매핑은 MasterBlSeaSubFactory, sub 엔티티 변환은 MasterBlSubFactory에 위임한다.
 */
@Component
public class MasterBlFactory {

    private final MasterBlSubFactory sub;
    private final MasterBlSeaSubFactory seaSubFactory;

    public MasterBlFactory(MasterBlSubFactory sub, MasterBlSeaSubFactory seaSubFactory) {
        this.sub = sub;
        this.seaSubFactory = seaSubFactory;
    }

    // ── CREATE ────────────────────────────────────────────────────────

    public MasterBl toEntity(CreateMasterBlCommand cmd) {
        MasterBl entity = MasterBlJobDiv.SEA.name().equals(cmd.jobDiv())
                ? MasterBlSea.create(Bound.valueOf(cmd.bound()))
                : MasterBlAir.create(Bound.valueOf(cmd.bound()));

        entity.assignMblNo(BlNumber.of(cmd.mblNo()), BlNumber.of(cmd.masterRefNo()));
        entity.assignParties(CustomerCode.of(cmd.shipperCode()), CustomerCode.of(cmd.consigneeCode()), CustomerCode.of(cmd.notifyCode()));
        entity.updateSchedule(PortCode.of(cmd.polCode()), PortCode.of(cmd.podCode()), BlDate.of(cmd.etd()), BlDate.of(cmd.eta()));
        entity.updateFreightAndOperator(Nullables.mapOrNull(cmd.freightTerm(), FreightTerm::valueOf), EmployeeCode.of(cmd.operatorCode()), TeamCode.of(cmd.teamCode()));
        if (cmd.shipmentType() != null) entity.updateShipmentType(Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf));
        entity.updateCargoSummary(new CargoSummary(Quantity.of(cmd.pkgQty()), cmd.pkgUnit(), Nullables.mapOrNull(cmd.weightUnit(), WeightUnit::fromCode), Weight.of(cmd.grossWeightKg()), Volume.of(cmd.cbm())));
        if (cmd.mainItemName() != null || cmd.hsCode() != null) entity.updateTradeInfo(cmd.mainItemName(), cmd.hsCode());
        if (cmd.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(cmd.settlePartnerCode()));

        if (cmd.seaDetail() != null) seaSubFactory.applySeaCreate(entity, cmd.seaDetail());
        applyRemark(entity, cmd.remark());
        sub.applySubEntities(entity, sub.toDescParams(cmd.desc()), sub.toDimParamsFromCreate(cmd.dims()), sub.toLegParamsFromCreate(cmd.scheduleLegs()), sub.toChargeParamsFromCreate(cmd.airCharges()));
        return entity;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    public void applyToEntity(UpdateMasterBlCommand cmd, MasterBl entity) {
        entity.assignMblNo(
                Nullables.mapOrElse(cmd.mblNo(), BlNumber::of, entity::getMblNo),
                Nullables.mapOrElse(cmd.masterRefNo(), BlNumber::of, entity::getMasterRefNo)
        );
        // §6.37 PATCH 의미론: null이면 기존 값 보존, non-null만 update
        if (cmd.shipperCode() != null || cmd.consigneeCode() != null || cmd.notifyCode() != null) {
            entity.assignParties(
                    Nullables.mapOrElse(cmd.shipperCode(), CustomerCode::of, entity::getShipperCode),
                    Nullables.mapOrElse(cmd.consigneeCode(), CustomerCode::of, entity::getConsigneeCode),
                    Nullables.mapOrElse(cmd.notifyCode(), CustomerCode::of, entity::getNotifyCode)
            );
        }
        entity.updateSchedule(
                Nullables.mapOrElse(cmd.polCode(), PortCode::of, entity::getPolCode),
                Nullables.mapOrElse(cmd.podCode(), PortCode::of, entity::getPodCode),
                Nullables.mapOrElse(cmd.etd(), BlDate::of, entity::getEtd),
                Nullables.mapOrElse(cmd.eta(), BlDate::of, entity::getEta)
        );
        entity.updateFreightAndOperator(
                Nullables.mapOrElse(cmd.freightTerm(), FreightTerm::valueOf, entity::getFreightTerm),
                Nullables.mapOrElse(cmd.operatorCode(), EmployeeCode::of, entity::getOperatorCode),
                Nullables.mapOrElse(cmd.teamCode(), TeamCode::of, entity::getTeamCode)
        );
        if (cmd.shipmentType() != null) entity.updateShipmentType(Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf));
        entity.updateCargoSummary(new CargoSummary(
                Nullables.mapOrElse(cmd.pkgQty(), Quantity::of, entity::getPkgQty),
                Nullables.firstNonNull(cmd.pkgUnit(), entity::getPkgUnit),
                Nullables.mapOrElse(cmd.weightUnit(), WeightUnit::fromCode, entity::getWeightUnit),
                Nullables.mapOrElse(cmd.grossWeightKg(), Weight::of, entity::getGrossWeightKg),
                Nullables.mapOrElse(cmd.cbm(), Volume::of, entity::getCbm)
        ));
        if (cmd.mainItemName() != null || cmd.hsCode() != null) {
            entity.updateTradeInfo(
                    Nullables.firstNonNull(cmd.mainItemName(), entity::getMainItemName),
                    Nullables.firstNonNull(cmd.hsCode(),       entity::getHsCode)
            );
        }
        if (cmd.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(cmd.settlePartnerCode()));

        if (cmd.seaDetail() != null) seaSubFactory.applySeaUpdate(entity, cmd.seaDetail());
        applyRemark(entity, cmd.remark());
        sub.applySubEntities(entity, sub.toDescParams(cmd.desc()), sub.toDimParams(cmd.dims()), sub.toLegParams(cmd.scheduleLegs()), sub.toChargeParams(cmd.airCharges()));
    }

    // ── SearchCommand → Domain Filter 변환 ───────────────────────────

    public MasterBlFilter toFilter(SearchMasterBlCommand cmd) {
        return new MasterBlFilter(
                Nullables.mapOrNull(cmd.bound(), Bound::valueOf),
                cmd.mblNo(),
                cmd.shipperCode(),
                cmd.consigneeCode(),
                cmd.polCode(),
                cmd.podCode(),
                cmd.etdFrom(),
                cmd.etdTo()
        );
    }

    // ── Entity → Projection 변환 ─────────────────────────────────────

    public MasterBlDetailResult toDetailResult(MasterBl entity, List<ConsoledHouseBlSummary> consolidatedHouseBls, List<ConsoledSeaContainer> containers) {
        String remark = switch (entity) {
            case MasterBlSea sea -> sea.getRemark();
            case MasterBlAir air -> air.getRemark();
            default -> null;
        };
        SeaDetailProjection seaDetail = switch (entity) {
            case MasterBlSea sea -> toSeaDetailProjection(sea);
            default -> null;
        };
        return new MasterBlDetailResult(
                entity.getId(),
                VoMapper.mapOrNull(entity.getMblNo(), BlNumber::value),
                VoMapper.mapOrNull(entity.getMasterRefNo(), BlNumber::value),
                Nullables.mapOrNull(entity.getJobDiv(), MasterBlJobDiv::name),
                Nullables.mapOrNull(entity.getBound(), Bound::name),
                Nullables.mapOrNull(entity.getShipmentType(), e -> e.name()),
                VoMapper.mapOrNull(entity.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getNotifyCode(), CustomerCode::value),
                Nullables.mapOrNull(entity.getShipperCode(), CustomerCode::address),
                Nullables.mapOrNull(entity.getConsigneeCode(), CustomerCode::address),
                Nullables.mapOrNull(entity.getNotifyCode(), CustomerCode::address),
                VoMapper.mapOrNull(entity.getPolCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getPodCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getEtd(), BlDate::asString),
                VoMapper.mapOrNull(entity.getEta(), BlDate::asString),
                Nullables.mapOrNull(entity.getFreightTerm(), FreightTerm::name),
                VoMapper.mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(entity.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(entity.getPkgQty(), Quantity::count),
                entity.getPkgUnit(),
                Nullables.mapOrNull(entity.getWeightUnit(), WeightUnit::name),
                VoMapper.mapOrNull(entity.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(entity.getCbm(), Volume::cbm),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                toConsoledViews(consolidatedHouseBls),
                toConsoledSeaContainerViews(containers),
                remark,
                seaDetail
        );
    }

    private SeaDetailProjection toSeaDetailProjection(MasterBlSea sea) {
        return new SeaDetailProjection(
                Nullables.mapOrNull(sea.getLoadType(), LoadType::name),
                VoMapper.mapOrNull(sea.getLinerCode(), LinerCode::value),
                sea.getVesselVoyage() != null ? sea.getVesselVoyage().vesselCode() : null,
                sea.getVesselVoyage() != null ? sea.getVesselVoyage().vesselName() : null,
                sea.getVesselVoyage() != null ? sea.getVesselVoyage().voyageNo() : null,
                VoMapper.mapOrNull(sea.getOnboardDate(), BlDate::asString),
                sea.getVesselNationality(),
                Nullables.mapOrNull(sea.getServiceTerm(), ServiceTerm::name),
                Nullables.mapOrNull(sea.getBlType(), BlType::name),
                VoMapper.mapOrNull(sea.getPorCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getFinalDestCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getRton(), Rton::ton),
                VoMapper.mapOrNull(sea.getLineBkgNo(), BlNumber::value),
                VoMapper.mapOrNull(sea.getIssueDate(), BlDate::asString),
                toSeaDescProjection(sea.getDesc()),
                sea.getRemark()
        );
    }

    private SeaDescProjection toSeaDescProjection(MasterBlDesc desc) {
        if (desc == null) return SeaDescProjection.empty();
        return new SeaDescProjection(
                desc.getMarks(),
                desc.getDescription(),
                Nullables.mapOrNull(desc.getDescClause1(), DescClause1::name),
                Nullables.mapOrNull(desc.getDescClause2(), DescClause2::name)
        );
    }

    // ── remark 매핑 ───────────────────────────────────────────────────

    private void applyRemark(MasterBl entity, String remark) {
        switch (entity) {
            case MasterBlSea sea -> sea.updateRemark(remark);
            case MasterBlAir air -> air.updateRemark(remark);
            default -> { /* 다른 타입은 remark 미지원 */ }
        }
    }

    // ── ConsoledHouseBlSummary → ConsoledHouseBlSummaryView 변환 ─────

    private List<ConsoledHouseBlSummaryView> toConsoledViews(List<ConsoledHouseBlSummary> sources) {
        if (sources == null) return List.of();
        return sources.stream().map(this::toConsoledView).toList();
    }

    private List<ConsoledSeaContainerView> toConsoledSeaContainerViews(List<ConsoledSeaContainer> sources) {
        if (sources == null) return List.of();
        return sources.stream().map(this::toConsoledSeaContainerView).toList();
    }

    private ConsoledSeaContainerView toConsoledSeaContainerView(ConsoledSeaContainer c) {
        return new ConsoledSeaContainerView(c.houseBlId(), c.containerNo(), c.containerType(), c.sealNo1(), c.sealNo2(), c.sealNo3(), c.pkgQty(), c.pkgUnit(), c.grossWeightKg(), c.cbm(), c.vgmKg());
    }

    private ConsoledHouseBlSummaryView toConsoledView(ConsoledHouseBlSummary summary) {
        return switch (summary) {
            case ConsoledHouseBlSeaSummary s -> new ConsoledHouseBlSummaryView(
                    s.houseBlId(), s.hblNo(), s.shipperCode(), s.consigneeCode(), s.docPartnerCode(),
                    s.pkgQty(), s.pkgUnit(), s.weightUnit(), s.grossWeightKg(), s.cbm(),
                    s.etd(), s.eta(), s.vesselName(), s.voyageNo(), s.polCode(), s.podCode(), null
            );
            case ConsoledHouseBlAirSummary a -> new ConsoledHouseBlSummaryView(
                    a.houseBlId(), a.hblNo(), a.shipperCode(), a.consigneeCode(), a.docPartnerCode(),
                    a.pkgQty(), a.pkgUnit(), a.weightUnit(), a.grossWeightKg(), a.cbm(),
                    null, null, null, null, null, null, a.chargeWeightKg()
            );
        };
    }
}
