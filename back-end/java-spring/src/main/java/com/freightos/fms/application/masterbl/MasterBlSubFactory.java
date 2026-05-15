package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAirCharge;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlDim;
import com.freightos.fms.domain.masterbl.entity.MasterBlScheduleLeg;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sub 엔티티(Desc, Dim, ScheduleLeg, AirCharge) 파라미터 변환 및 생성 담당.
 * MasterBlFactory 크기 분리를 위해 sub 엔티티 관련 메서드를 이 클래스에 위임한다.
 */
@Component
class MasterBlSubFactory {

    // ── Desc 파라미터 변환 ────────────────────────────────────────────

    String[] toDescParams(CreateMasterBlCommand.DescCommand r) {
        if (r == null) return null;
        return new String[]{ r.marks(), r.description(), r.descClause1(), r.descClause2() };
    }

    String[] toDescParams(UpdateMasterBlCommand.DescCommand r) {
        if (r == null) return null;
        return new String[]{ r.marks(), r.description(), r.descClause1(), r.descClause2() };
    }

    // ── Dim 파라미터 변환 ─────────────────────────────────────────────

    List<DimParams> toDimParamsFromCreate(List<CreateMasterBlCommand.DimCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new DimParams(r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    List<DimParams> toDimParams(List<UpdateMasterBlCommand.DimCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new DimParams(r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    // ── ScheduleLeg 파라미터 변환 ─────────────────────────────────────

    List<LegParams> toLegParamsFromCreate(List<CreateMasterBlCommand.ScheduleLegCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new LegParams(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    List<LegParams> toLegParams(List<UpdateMasterBlCommand.ScheduleLegCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new LegParams(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    // ── AirCharge 파라미터 변환 ───────────────────────────────────────

    List<ChargeParams> toChargeParamsFromCreate(List<CreateMasterBlCommand.AirChargeCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new ChargeParams(r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    List<ChargeParams> toChargeParams(List<UpdateMasterBlCommand.AirChargeCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return List.of();
        return cmds.stream().map(r -> new ChargeParams(r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── Sub 엔티티 실제 생성 ──────────────────────────────────────────

    void applySubEntities(MasterBl entity, String[] descParams,
                          List<DimParams> dimParams, List<LegParams> legParams,
                          List<ChargeParams> chargeParams) {
        if (descParams != null) {
            MasterBlDesc desc = MasterBlDesc.create(null);
            desc.updateContent(descParams[0], descParams[1],
                    DescClause1.fromCode(descParams[2]), DescClause2.fromCode(descParams[3]));
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
                        Nullables.mapOrNull(r.freightTerm(), FreightTerm::valueOf),
                        Weight.of(r.grossWt()), RateClass.fromCode(r.rateClass()), Weight.of(r.chargeWt()), r.rate()
                ));
                return charge;
            }).toList());
        }
    }

    // ── 내부 파라미터 record ─────────────────────────────────────────

    record DimParams(BigDecimal l, BigDecimal w, BigDecimal h, Integer qty, BigDecimal cbm, BigDecimal vwKg) {}
    record LegParams(String toCode, String byCarrier, String flightNo, String onBoardDt, String onBoardTm, String arrivalDt, String arrivalTm) {}
    record ChargeParams(String freightCode, String currencyCode, String per, String freightTerm, BigDecimal grossWt, String rateClass, BigDecimal chargeWt, BigDecimal rate) {}
}
