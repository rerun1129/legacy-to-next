package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.TruckType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * HouseBlAssembler에서 위임받아 sub 엔티티(Desc/Dim/Container/ScheduleLeg/License/TruckOrder/AirCharge)를
 * 도메인 엔티티로 변환하는 보조 컴포넌트.
 */
@Component
public class HouseBlSubAssembler {

    // ── Desc ─────────────────────────────────────────────────────────

    public void applyDesc(HouseBl entity, CreateHouseBlRequest.DescRequest r) {
        if (r == null) return;
        HouseBlDesc desc = HouseBlDesc.create(null);
        desc.updateContent(r.marks(), r.description(),
                DescClause1.fromCode(r.descClause1()), DescClause2.fromCode(r.descClause2()), r.remark());
        entity.initDesc(desc);
    }

    public void applyDescUpdate(HouseBl entity, UpdateHouseBlRequest.DescRequest r) {
        if (r == null) return;
        HouseBlDesc desc = HouseBlDesc.create(null);
        desc.updateContent(r.marks(), r.description(),
                DescClause1.fromCode(r.descClause1()), DescClause2.fromCode(r.descClause2()), r.remark());
        entity.initDesc(desc);
    }

    // ── Dims ──────────────────────────────────────────────────────────

    public void applyDims(HouseBl entity, List<CreateHouseBlRequest.DimRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        entity.initDims(reqs.stream()
                .map(r -> HouseBlDim.create(null,
                        r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg()))
                .toList());
    }

    public void applyDimsUpdate(HouseBl entity, List<UpdateHouseBlRequest.DimRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        entity.initDims(reqs.stream()
                .map(r -> HouseBlDim.create(null,
                        r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg()))
                .toList());
    }

    // ── Container ────────────────────────────────────────────────────

    /** 변환된 컨테이너 파라미터. DTO 타입에 독립적인 중간 표현. */
    public record ContainerParams(
            String containerNo, String containerType, Integer lengthFeet,
            String sealNo1, String sealNo2, String sealNo3,
            String sealNo4, String sealNo5, String sealNo6,
            Integer pkgQty, String pkgUnit,
            BigDecimal grossWeightKg, BigDecimal netWeightKg, BigDecimal cbm,
            BigDecimal vgmKg, boolean soc, int seq) {}

    public void applyContainers(HouseBl entity, List<ContainerParams> params) {
        if (params == null || params.isEmpty()) return;
        entity.initContainers(params.stream().map(p -> {
            HouseBlContainer c = HouseBlContainer.of(
                    entity, ContainerNumber.of(p.containerNo()),
                    ContainerType.fromCode(p.containerType()),
                    p.lengthFeet() != null ? p.lengthFeet() : 20);
            c.updateDetails(new HouseBlContainer.Details(
                    SealNumber.of(p.sealNo1()), SealNumber.of(p.sealNo2()), SealNumber.of(p.sealNo3()),
                    SealNumber.of(p.sealNo4()), SealNumber.of(p.sealNo5()), SealNumber.of(p.sealNo6()),
                    Quantity.of(p.pkgQty()), WeightUnit.fromCode(p.pkgUnit()),
                    Weight.of(p.grossWeightKg()), Weight.of(p.netWeightKg()),
                    Volume.of(p.cbm()), Weight.of(p.vgmKg()), p.soc(), p.seq()));
            return c;
        }).toList());
    }

    // ── ScheduleLegs ─────────────────────────────────────────────────

    public void applyScheduleLegs(HouseBl entity,
                                   List<CreateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        entity.initScheduleLegs(reqs.stream().map(r -> {
            HouseBlScheduleLeg leg = HouseBlScheduleLeg.create(
                    null, r.toCode(), r.onBoardDt(), r.arrivalDt());
            leg.updateDetails(r.toCode(), r.byCarrier(), r.flightNo(),
                    r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm());
            return leg;
        }).toList());
    }

    public void applyScheduleLegsUpdate(HouseBl entity,
                                         List<UpdateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        entity.initScheduleLegs(reqs.stream().map(r -> {
            HouseBlScheduleLeg leg = HouseBlScheduleLeg.create(
                    null, r.toCode(), r.onBoardDt(), r.arrivalDt());
            leg.updateDetails(r.toCode(), r.byCarrier(), r.flightNo(),
                    r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm());
            return leg;
        }).toList());
    }

    // ── TruckOrder ───────────────────────────────────────────────────

    /** 변환된 트럭 오더 파라미터. */
    public record TruckOrderParams(
            String truckOrderNo, Integer pkgQty, String pkgUnit,
            BigDecimal grossWeightKg, BigDecimal cbm,
            String truckNo, String truckType, String driver, String mobileNo,
            String containerNo, String containerType,
            String sealNo1, String sealNo2, String sealNo3) {}

    public void applyTruckOrders(HouseBl entity, List<TruckOrderParams> params) {
        if (params == null || params.isEmpty()) return;
        entity.initTruckOrders(params.stream().map(p -> {
            HouseBlTruckOrder o = HouseBlTruckOrder.create(null);
            o.updateDetails(new HouseBlTruckOrder.Details(
                    p.truckOrderNo(), p.pkgQty(), p.pkgUnit(),
                    Weight.of(p.grossWeightKg()), Volume.of(p.cbm()),
                    p.truckNo(), TruckType.fromLabel(p.truckType()), p.driver(), p.mobileNo(),
                    ContainerNumber.of(p.containerNo()), ContainerType.fromCode(p.containerType()),
                    SealNumber.of(p.sealNo1()), SealNumber.of(p.sealNo2()), SealNumber.of(p.sealNo3())));
            return o;
        }).toList());
    }

    // ── 일괄 적용 진입점 ─────────────────────────────────────────────

    /** CREATE 요청에서 모든 sub 엔티티를 일괄 적용한다. */
    public void applyAllCreate(HouseBl entity, CreateHouseBlRequest req) {
        applyDesc(entity, req.desc());
        applyDims(entity, req.dims());
        applyContainers(entity, toContainerParams(req.containers()));
        applyScheduleLegs(entity, req.scheduleLegs());
        applyTruckOrders(entity, toTruckOrderParams(req.truckOrders()));
        applyAirCharges(entity, req.airCharges());
    }

    /** UPDATE 요청에서 모든 sub 엔티티를 일괄 적용한다. */
    public void applyAllUpdate(HouseBl entity, UpdateHouseBlRequest req) {
        applyDescUpdate(entity, req.desc());
        applyDimsUpdate(entity, req.dims());
        applyContainers(entity, toContainerParamsU(req.containers()));
        applyScheduleLegsUpdate(entity, req.scheduleLegs());
        applyTruckOrders(entity, toTruckOrderParamsU(req.truckOrders()));
        applyAirChargesUpdate(entity, req.airCharges());
    }

    private List<ContainerParams> toContainerParams(List<CreateHouseBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream().map(c -> new ContainerParams(
                c.containerNo(), c.containerType(), c.lengthFeet(),
                c.sealNo1(), c.sealNo2(), c.sealNo3(), c.sealNo4(), c.sealNo5(), c.sealNo6(),
                c.pkgQty(), c.pkgUnit(), c.grossWeightKg(), c.netWeightKg(), c.cbm(),
                c.vgmKg(), Boolean.TRUE.equals(c.soc()), c.seq() != null ? c.seq() : 1)).toList();
    }

    private List<ContainerParams> toContainerParamsU(List<UpdateHouseBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream().map(c -> new ContainerParams(
                c.containerNo(), c.containerType(), c.lengthFeet(),
                c.sealNo1(), c.sealNo2(), c.sealNo3(), c.sealNo4(), c.sealNo5(), c.sealNo6(),
                c.pkgQty(), c.pkgUnit(), c.grossWeightKg(), c.netWeightKg(), c.cbm(),
                c.vgmKg(), Boolean.TRUE.equals(c.soc()), c.seq() != null ? c.seq() : 1)).toList();
    }

    private List<TruckOrderParams> toTruckOrderParams(List<CreateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream().map(t -> new TruckOrderParams(
                t.truckOrderNo(), t.pkgQty(), t.pkgUnit(), t.grossWeightKg(), t.cbm(),
                t.truckNo(), t.truckType(), t.driver(), t.mobileNo(),
                t.containerNo(), t.containerType(), t.sealNo1(), t.sealNo2(), t.sealNo3())).toList();
    }

    private List<TruckOrderParams> toTruckOrderParamsU(List<UpdateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream().map(t -> new TruckOrderParams(
                t.truckOrderNo(), t.pkgQty(), t.pkgUnit(), t.grossWeightKg(), t.cbm(),
                t.truckNo(), t.truckType(), t.driver(), t.mobileNo(),
                t.containerNo(), t.containerType(), t.sealNo1(), t.sealNo2(), t.sealNo3())).toList();
    }

    // ── AirCharges ───────────────────────────────────────────────────

    public void applyAirCharges(HouseBl entity,
                                 List<CreateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        entity.initAirCharges(reqs.stream().map(r -> {
            HouseBlAirCharge c = HouseBlAirCharge.create(null);
            c.updateDetails(new HouseBlAirCharge.Details(
                    r.freightCode(), CurrencyCode.of(r.currencyCode()), Per.fromCode(r.per()),
                    r.freightTerm() != null ? FreightTerm.valueOf(r.freightTerm()) : null,
                    Weight.of(r.grossWeightKg()), RateClass.fromCode(r.rateClass()),
                    Weight.of(r.chargeWeightKg()), r.rate()));
            return c;
        }).toList());
    }

    public void applyAirChargesUpdate(HouseBl entity,
                                       List<UpdateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        entity.initAirCharges(reqs.stream().map(r -> {
            HouseBlAirCharge c = HouseBlAirCharge.create(null);
            c.updateDetails(new HouseBlAirCharge.Details(
                    r.freightCode(), CurrencyCode.of(r.currencyCode()), Per.fromCode(r.per()),
                    r.freightTerm() != null ? FreightTerm.valueOf(r.freightTerm()) : null,
                    Weight.of(r.grossWeightKg()), RateClass.fromCode(r.rateClass()),
                    Weight.of(r.chargeWeightKg()), r.rate()));
            return c;
        }).toList());
    }
}
