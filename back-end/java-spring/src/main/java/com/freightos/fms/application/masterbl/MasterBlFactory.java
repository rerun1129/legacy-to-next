package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CargoSummary;
import com.freightos.fms.domain.common.vo.CurrencyCode;
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
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlAirCharge;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlDim;
import com.freightos.fms.domain.masterbl.entity.MasterBlScheduleLeg;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Command → 도메인 Entity 변환 팩토리.
 * Adapter(in)에서 분리된 VO/Entity 생성 로직을 이 계층에 집중한다.
 */
@Component
public class MasterBlFactory {

    // ── CREATE ────────────────────────────────────────────────────────

    public MasterBl toEntity(CreateMasterBlCommand cmd) {
        MasterBl entity = cmd.jobDiv() == MasterBlJobDiv.SEA
                ? MasterBlSea.create(cmd.bound())
                : MasterBlAir.create(cmd.bound());

        entity.assignMblNo(BlNumber.of(cmd.mblNo()), BlNumber.of(cmd.masterRefNo()));
        entity.assignParties(CustomerCode.of(cmd.shipperCode()), CustomerCode.of(cmd.consigneeCode()), CustomerCode.of(cmd.notifyCode()));
        entity.updateSchedule(PortCode.of(cmd.polCode()), PortCode.of(cmd.podCode()), BlDate.of(cmd.etd()), BlDate.of(cmd.eta()));
        entity.updateFreightAndOperator(cmd.freightTerm(), EmployeeCode.of(cmd.operatorCode()), null);
        entity.updateCargoSummary(new CargoSummary(Quantity.of(cmd.pkgQty()), WeightUnit.fromCode(cmd.pkgUnit()), Weight.of(cmd.grossWeightKg()), Volume.of(cmd.cbm())));
        if (cmd.mainItemName() != null || cmd.hsCode() != null) entity.updateTradeInfo(cmd.mainItemName(), cmd.hsCode());
        if (cmd.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(cmd.settlePartnerCode()));

        if (cmd.seaDetail() != null) applySeaCreate(entity, cmd.seaDetail());
        applySubEntities(entity, toDescParams(cmd.desc()), toDimParamsFromCreate(cmd.dims()), toLegParamsFromCreate(cmd.scheduleLegs()), toChargeParamsFromCreate(cmd.airCharges()));
        return entity;
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    public void applyToEntity(UpdateMasterBlCommand cmd, MasterBl entity) {
        entity.assignMblNo(
                cmd.mblNo()       != null ? BlNumber.of(cmd.mblNo())       : entity.getMblNo(),
                cmd.masterRefNo() != null ? BlNumber.of(cmd.masterRefNo()) : entity.getMasterRefNo()
        );
        entity.assignParties(
                cmd.shipperCode()   != null ? CustomerCode.of(cmd.shipperCode())   : entity.getShipperCode(),
                cmd.consigneeCode() != null ? CustomerCode.of(cmd.consigneeCode()) : entity.getConsigneeCode(),
                cmd.notifyCode()    != null ? CustomerCode.of(cmd.notifyCode())    : entity.getNotifyCode()
        );
        entity.updateSchedule(
                cmd.polCode() != null ? PortCode.of(cmd.polCode()) : entity.getPolCode(),
                cmd.podCode() != null ? PortCode.of(cmd.podCode()) : entity.getPodCode(),
                cmd.etd()     != null ? BlDate.of(cmd.etd())       : entity.getEtd(),
                cmd.eta()     != null ? BlDate.of(cmd.eta())       : entity.getEta()
        );
        entity.updateFreightAndOperator(
                cmd.freightTerm()  != null ? cmd.freightTerm()                    : entity.getFreightTerm(),
                cmd.operatorCode() != null ? EmployeeCode.of(cmd.operatorCode())  : entity.getOperatorCode(),
                entity.getTeamCode()
        );
        entity.updateCargoSummary(new CargoSummary(
                cmd.pkgQty()        != null ? Quantity.of(cmd.pkgQty())           : entity.getPkgQty(),
                cmd.pkgUnit()       != null ? WeightUnit.fromCode(cmd.pkgUnit())  : entity.getPkgUnit(),
                cmd.grossWeightKg() != null ? Weight.of(cmd.grossWeightKg())      : entity.getGrossWeightKg(),
                cmd.cbm()           != null ? Volume.of(cmd.cbm())                : entity.getCbm()
        ));
        if (cmd.mainItemName() != null || cmd.hsCode() != null) {
            entity.updateTradeInfo(
                    cmd.mainItemName() != null ? cmd.mainItemName() : entity.getMainItemName(),
                    cmd.hsCode()       != null ? cmd.hsCode()       : entity.getHsCode()
            );
        }
        if (cmd.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(cmd.settlePartnerCode()));

        if (cmd.seaDetail() != null) applySeaUpdate(entity, cmd.seaDetail());
        applySubEntities(entity, toDescParams(cmd.desc()), toDimParams(cmd.dims()), toLegParams(cmd.scheduleLegs()), toChargeParams(cmd.airCharges()));
    }

    // ── SearchCommand → Domain Filter 변환 ───────────────────────────

    public MasterBlFilter toFilter(SearchMasterBlCommand cmd) {
        return new MasterBlFilter(
                cmd.bound() != null ? Bound.valueOf(cmd.bound()) : null,
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

    public MasterBlDetailResult toDetailResult(MasterBl entity, List<ConsoledHouseBlSummary> consolidatedHouseBls) {
        return new MasterBlDetailResult(
                entity.getId(),
                VoMapper.mapOrNull(entity.getMblNo(), BlNumber::value),
                VoMapper.mapOrNull(entity.getMasterRefNo(), BlNumber::value),
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipmentType(),
                VoMapper.mapOrNull(entity.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getPolCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getPodCode(), PortCode::value),
                VoMapper.mapOrNull(entity.getEtd(), BlDate::asString),
                VoMapper.mapOrNull(entity.getEta(), BlDate::asString),
                entity.getFreightTerm(),
                VoMapper.mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(entity.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(entity.getPkgQty(), Quantity::count),
                entity.getPkgUnit(),
                VoMapper.mapOrNull(entity.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(entity.getCbm(), Volume::cbm),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                consolidatedHouseBls
        );
    }

    // ── SEA 확장 필드 매핑 ────────────────────────────────────────────

    private void applySeaCreate(MasterBl entity, CreateMasterBlCommand.SeaDetailCommand s) {
        if (!(entity instanceof MasterBlSea sea)) return;
        sea.updateSeaFields(
                s.loadType() != null ? LoadType.valueOf(s.loadType()) : null,
                LinerCode.of(s.linerCode()),
                VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo()),
                BlDate.of(s.onboardDate()), BlNumber.of(s.lineBkgNo()), BlDate.of(s.issueDate())
        );
        applySeaCommon(sea, s.vesselNationality(), s.weightUnit(), s.serviceTerm(), s.blType(), s.porCode(), s.finalDestCode(), s.rton());
    }

    private void applySeaUpdate(MasterBl entity, UpdateMasterBlCommand.SeaDetailCommand s) {
        if (!(entity instanceof MasterBlSea sea)) return;
        sea.updateSeaFields(
                s.loadType()    != null ? LoadType.valueOf(s.loadType())                                  : sea.getLoadType(),
                s.linerCode()   != null ? LinerCode.of(s.linerCode())                                    : sea.getLinerCode(),
                s.vesselName()  != null ? VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo())  : sea.getVesselVoyage(),
                s.onboardDate() != null ? BlDate.of(s.onboardDate())                                     : sea.getOnboardDate(),
                s.lineBkgNo()   != null ? BlNumber.of(s.lineBkgNo())                                     : sea.getLineBkgNo(),
                s.issueDate()   != null ? BlDate.of(s.issueDate())                                       : sea.getIssueDate()
        );
        applySeaCommon(sea, s.vesselNationality(), s.weightUnit(), s.serviceTerm(), s.blType(), s.porCode(), s.finalDestCode(), s.rton());
    }

    private void applySeaCommon(MasterBlSea sea, String vesselNationality, String weightUnit,
                                String serviceTerm, String blType, String porCode, String finalDestCode, BigDecimal rton) {
        if (vesselNationality != null) sea.updateVesselNationality(vesselNationality);
        if (weightUnit != null)        sea.updateWeightUnit(WeightUnit.fromCode(weightUnit));
        if (serviceTerm != null)       sea.updateServiceTerm(ServiceTerm.fromLabel(serviceTerm));
        if (blType != null)            sea.updateBlType(BlType.valueOf(blType));
        if (porCode != null || finalDestCode != null) sea.updateRoute(PortCode.of(porCode), PortCode.of(finalDestCode));
        if (rton != null)              sea.updateRton(Rton.of(rton));
    }

    // ── Sub 엔티티 파라미터 변환 ──────────────────────────────────────

    private String[] toDescParams(CreateMasterBlCommand.DescCommand r) {
        if (r == null) return null;
        return new String[]{ r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark() };
    }

    private String[] toDescParams(UpdateMasterBlCommand.DescCommand r) {
        if (r == null) return null;
        return new String[]{ r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark() };
    }

    private List<DimParams> toDimParamsFromCreate(List<CreateMasterBlCommand.DimCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new DimParams(r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<DimParams> toDimParams(List<UpdateMasterBlCommand.DimCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new DimParams(r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<LegParams> toLegParamsFromCreate(List<CreateMasterBlCommand.ScheduleLegCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new LegParams(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<LegParams> toLegParams(List<UpdateMasterBlCommand.ScheduleLegCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new LegParams(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<ChargeParams> toChargeParamsFromCreate(List<CreateMasterBlCommand.AirChargeCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new ChargeParams(r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    private List<ChargeParams> toChargeParams(List<UpdateMasterBlCommand.AirChargeCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new ChargeParams(r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── Sub 엔티티 실제 생성 ──────────────────────────────────────────

    private void applySubEntities(MasterBl entity, String[] descParams,
                                  List<DimParams> dimParams, List<LegParams> legParams,
                                  List<ChargeParams> chargeParams) {
        if (descParams != null) {
            MasterBlDesc desc = MasterBlDesc.create(null);
            desc.updateContent(descParams[0], descParams[1],
                    DescClause1.fromCode(descParams[2]), DescClause2.fromCode(descParams[3]), descParams[4]);
            entity.initDesc(desc);
        }
        if (!dimParams.isEmpty()) {
            entity.initDims(dimParams.stream()
                    .map(r -> MasterBlDim.create(null, r.l(), r.w(), r.h(), r.qty(), r.cbm(), r.vwKg())).toList());
        }
        if (!legParams.isEmpty()) {
            entity.initScheduleLegs(legParams.stream().map(r -> {
                MasterBlScheduleLeg leg = MasterBlScheduleLeg.create(null, r.toCode(), r.onBoardDt(), r.arrivalDt());
                leg.updateDetails(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm());
                return leg;
            }).toList());
        }
        if (!chargeParams.isEmpty()) {
            entity.initAirCharges(chargeParams.stream().map(r -> {
                MasterBlAirCharge charge = MasterBlAirCharge.create(null);
                charge.updateDetails(new MasterBlAirCharge.Details(
                        r.freightCode(), CurrencyCode.of(r.currencyCode()), Per.fromCode(r.per()),
                        r.freightTerm() != null ? FreightTerm.valueOf(r.freightTerm()) : null,
                        Weight.of(r.grossWt()), RateClass.fromCode(r.rateClass()), Weight.of(r.chargeWt()), r.rate()
                ));
                return charge;
            }).toList());
        }
    }

    // ── 내부 파라미터 record ─────────────────────────────────────────

    private record DimParams(BigDecimal l, BigDecimal w, BigDecimal h, Integer qty, BigDecimal cbm, BigDecimal vwKg) {}
    private record LegParams(String toCode, String byCarrier, String flightNo, String onBoardDt, String onBoardTm, String arrivalDt, String arrivalTm) {}
    private record ChargeParams(String freightCode, String currencyCode, String per, String freightTerm, BigDecimal grossWt, String rateClass, BigDecimal chargeWt, BigDecimal rate) {}
}
