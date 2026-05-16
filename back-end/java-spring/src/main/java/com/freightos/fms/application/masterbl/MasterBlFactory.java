package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.AirChargeProjection;
import com.freightos.fms.application.masterbl.projection.AirDetailProjection;
import com.freightos.fms.application.masterbl.projection.ConsoledHouseBlSummaryView;
import com.freightos.fms.application.masterbl.projection.ConsoledSeaContainerView;
import com.freightos.fms.application.masterbl.projection.DescProjection;
import com.freightos.fms.application.masterbl.projection.DimProjection;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.ScheduleLegProjection;
import com.freightos.fms.application.masterbl.projection.SeaDetailProjection;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CargoSummary;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
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
    private final MasterBlAirSubFactory airSubFactory;

    public MasterBlFactory(MasterBlSubFactory sub, MasterBlSeaSubFactory seaSubFactory, MasterBlAirSubFactory airSubFactory) {
        this.sub = sub;
        this.seaSubFactory = seaSubFactory;
        this.airSubFactory = airSubFactory;
    }

    // ── CREATE ────────────────────────────────────────────────────────

    public MasterBl toEntity(CreateMasterBlCommand cmd) {
        MasterBl entity = MasterBlJobDiv.SEA.name().equals(cmd.jobDiv())
                ? MasterBlSea.create(Bound.valueOf(cmd.bound()))
                : MasterBlAir.create(Bound.valueOf(cmd.bound()));

        entity.assignMblNo(BlNumber.of(cmd.mblNo()), BlNumber.of(cmd.masterRefNo()));
        // CustomerCode.of(value, address) 2-arg로 address도 함께 반영 (1-arg는 address 누락 버그).
        entity.assignParties(
                CustomerCode.of(cmd.shipperCode(),   cmd.shipperAddress()),
                CustomerCode.of(cmd.consigneeCode(), cmd.consigneeAddress()),
                CustomerCode.of(cmd.notifyCode(),    cmd.notifyAddress())
        );
        entity.updateSchedule(PortCode.of(cmd.polCode()), PortCode.of(cmd.podCode()), BlDate.of(cmd.etd()), BlDate.of(cmd.eta()));
        entity.updateFreightAndOperator(Nullables.mapOrNull(cmd.freightTerm(), FreightTerm::valueOf), EmployeeCode.of(cmd.operatorCode()), TeamCode.of(cmd.teamCode()));
        if (cmd.shipmentType() != null) entity.updateShipmentType(Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf));
        entity.updateCargoSummary(new CargoSummary(Quantity.of(cmd.pkgQty()), cmd.pkgUnit(), Nullables.mapOrNull(cmd.weightUnit(), WeightUnit::fromCode), Weight.of(cmd.grossWeightKg()), Volume.of(cmd.cbm())));
        if (cmd.mainItemName() != null || cmd.hsCode() != null) entity.updateTradeInfo(cmd.mainItemName(), cmd.hsCode());
        if (cmd.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(cmd.settlePartnerCode()));

        if (cmd.seaDetail() != null) seaSubFactory.applySeaCreate(entity, cmd.seaDetail());
        if (cmd.airDetail() != null) airSubFactory.applyAirCreate(entity, cmd.airDetail());
        // airDetail.remark()가 AIR 전용 채널로 올 때를 대비한 fallback (공통 remark 우선)
        String effectiveRemark = cmd.remark() != null ? cmd.remark() : (cmd.airDetail() != null ? cmd.airDetail().remark() : null);
        applyRemark(entity, effectiveRemark);
        sub.applySubEntities(entity, sub.toDescParams(cmd.desc()), sub.toDimParamsFromCreate(cmd.dims()), sub.toLegParamsFromCreate(cmd.scheduleLegs()), sub.toChargeParamsFromCreate(cmd.airCharges()));
        return entity;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    public void applyToEntity(UpdateMasterBlCommand cmd, MasterBl entity) {
        // mblNo·masterRefNo는 ChangeMasterBlNoCommand 전용 경로로만 변경 — 일반 UPDATE에서 제외.
        // §6.37 PATCH 의미론: code/address 중 하나라도 non-null이면 update, 둘 다 null이면 기존 값 보존.
        // CustomerCode.of(value, address) 2-arg 사용으로 address도 함께 도메인에 반영한다.
        // (1-arg method reference는 address를 항상 null로 덮어쓰는 버그가 있다.)
        boolean shipperTouched   = cmd.shipperCode()   != null || cmd.shipperAddress()   != null;
        boolean consigneeTouched = cmd.consigneeCode() != null || cmd.consigneeAddress() != null;
        boolean notifyTouched    = cmd.notifyCode()    != null || cmd.notifyAddress()    != null;
        if (shipperTouched || consigneeTouched || notifyTouched) {
            entity.assignParties(
                    shipperTouched
                            ? CustomerCode.of(cmd.shipperCode(),   cmd.shipperAddress())
                            : entity.getShipperCode(),
                    consigneeTouched
                            ? CustomerCode.of(cmd.consigneeCode(), cmd.consigneeAddress())
                            : entity.getConsigneeCode(),
                    notifyTouched
                            ? CustomerCode.of(cmd.notifyCode(),    cmd.notifyAddress())
                            : entity.getNotifyCode()
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
        if (cmd.airDetail() != null) airSubFactory.applyAirUpdate(entity, cmd.airDetail());
        // airDetail.remark()가 AIR 전용 채널로 올 때를 대비한 fallback (공통 remark 우선)
        String effectiveRemark = cmd.remark() != null ? cmd.remark() : (cmd.airDetail() != null ? cmd.airDetail().remark() : null);
        applyRemark(entity, effectiveRemark);
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
            case MasterBlSea sea -> seaSubFactory.toSeaDetailProjection(sea);
            default -> null;
        };
        AirDetailProjection airDetail = switch (entity) {
            case MasterBlAir air -> airSubFactory.toAirDetailProjection(air);
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
                entity.getMainItemName(),
                entity.getHsCode(),
                VoMapper.mapOrNull(entity.getSettlePartnerCode(), CustomerCode::value),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                toConsoledViews(consolidatedHouseBls),
                toConsoledSeaContainerViews(containers),
                remark,
                toDescProjection(entity.getDesc()),
                seaDetail,
                airDetail,
                toDimProjections(entity),
                toScheduleLegProjections(entity),
                toAirChargeProjections(entity)
        );
    }

    private DescProjection toDescProjection(MasterBlDesc desc) {
        if (desc == null) return DescProjection.empty();
        return new DescProjection(
                desc.getMarks(),
                desc.getDescription(),
                Nullables.mapOrNull(desc.getDescClause1(), DescClause1::name),
                Nullables.mapOrNull(desc.getDescClause2(), DescClause2::name)
        );
    }

    private List<DimProjection> toDimProjections(MasterBl entity) {
        return switch (entity) {
            case MasterBlAir ignored -> entity.getDims().stream()
                    .map(d -> new DimProjection(d.getLengthCm(), d.getWidthCm(), d.getHeightCm(), d.getQuantity(), d.getCbm(), d.getVolumeWeightKg()))
                    .toList();
            default -> List.of();
        };
    }

    private List<ScheduleLegProjection> toScheduleLegProjections(MasterBl entity) {
        return switch (entity) {
            case MasterBlAir ignored -> entity.getScheduleLegs().stream()
                    .map(l -> new ScheduleLegProjection(l.getToCode(), l.getByCarrier(), l.getFlightNo(), l.getOnBoardDt(), l.getOnBoardTm(), l.getArrivalDt(), l.getArrivalTm()))
                    .toList();
            default -> List.of();
        };
    }

    private List<AirChargeProjection> toAirChargeProjections(MasterBl entity) {
        return switch (entity) {
            case MasterBlAir ignored -> entity.getAirCharges().stream()
                    .map(c -> new AirChargeProjection(
                            c.getFreightCode(),
                            VoMapper.mapOrNull(c.getCurrencyCode(), CurrencyCode::value),
                            Nullables.mapOrNull(c.getPer(), Per::name),
                            Nullables.mapOrNull(c.getFreightTerm(), FreightTerm::name),
                            VoMapper.mapOrNull(c.getGrossWeightKg(), Weight::kg),
                            Nullables.mapOrNull(c.getRateClass(), RateClass::name),
                            VoMapper.mapOrNull(c.getChargeWeightKg(), Weight::kg),
                            c.getRate()
                    ))
                    .toList();
            default -> List.of();
        };
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
