package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlAirCharge;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlDim;
import com.freightos.fms.domain.housebl.entity.HouseBlScheduleLeg;
import com.freightos.fms.domain.housebl.entity.HouseBlTruckOrder;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.TruckType;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sub 엔티티(Desc, Dim, Container, ScheduleLeg, TruckOrder, AirCharge) Create/Update 변환 담당.
 * HouseBlFactory의 크기 분리를 위해 sub 엔티티 관련 12개 메서드를 이 클래스에 위임한다.
 */
@Component
class HouseBlSubFactory {

    // ── Desc ─────────────────────────────────────────────────────────

    void applyDesc(HouseBl entity, CreateHouseBlCommand.DescCommand r) {
        // NON_BL은 desc를 사용하지 않음 — remark는 본체 sub 엔티티 컬럼으로 관리됨
        if (entity instanceof HouseBlNonBl) return;
        if (r == null) return;
        HouseBlDesc desc = HouseBlDesc.create(null);
        desc.updateContent(r.marks(), r.description(),
                DescClause1.fromCode(r.descClause1()), DescClause2.fromCode(r.descClause2()));
        entity.initDesc(desc);
    }

    void applyDescUpdate(HouseBl entity, UpdateHouseBlCommand.DescCommand r) {
        // NON_BL은 desc를 사용하지 않음 — remark는 본체 sub 엔티티 컬럼으로 관리됨
        if (entity instanceof HouseBlNonBl) return;
        if (r == null) return;
        HouseBlDesc desc = HouseBlDesc.create(null);
        if (r.id() != null) desc.assignIdentity(r.id(), null, null, null, null);
        desc.updateContent(r.marks(), r.description(),
                DescClause1.fromCode(r.descClause1()), DescClause2.fromCode(r.descClause2()));
        entity.initDesc(desc);
    }

    // ── Dims ──────────────────────────────────────────────────────────

    void applyDims(HouseBl entity, List<CreateHouseBlCommand.DimCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initDims(cmds.stream()
                .map(c -> HouseBlDim.create(null,
                        c.lengthCm(), c.widthCm(), c.heightCm(), c.quantity(), c.cbm(), c.volumeWeightKg()))
                .toList());
    }

    void applyDimsUpdate(HouseBl entity, List<UpdateHouseBlCommand.DimCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initDims(cmds.stream().map(c -> {
            HouseBlDim dim = HouseBlDim.create(null, c.lengthCm(), c.widthCm(), c.heightCm(), c.quantity(), c.cbm(), c.volumeWeightKg());
            if (c.id() != null) dim.assignIdentity(c.id(), null, null, null, null);
            return dim;
        }).toList());
    }

    // ── Containers ───────────────────────────────────────────────────

    void applyContainers(HouseBl entity, List<CreateHouseBlCommand.ContainerCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initContainers(cmds.stream().map(c -> {
            HouseBlContainer container = HouseBlContainer.of(
                    entity, ContainerNumber.of(c.containerNo()),
                    ContainerType.fromCode(c.containerType()),
                    Nullables.firstNonNull(c.lengthFeet(), () -> 20));
            container.updateDetails(new HouseBlContainer.Details(
                    SealNumber.of(c.sealNo1()), SealNumber.of(c.sealNo2()), SealNumber.of(c.sealNo3()),
                    SealNumber.of(c.sealNo4()), SealNumber.of(c.sealNo5()), SealNumber.of(c.sealNo6()),
                    Quantity.of(c.pkgQty()), c.pkgUnit(),
                    Weight.of(c.grossWeightKg()), Weight.of(c.netWeightKg()),
                    Volume.of(c.cbm()), Weight.of(c.vgmKg()), Boolean.TRUE.equals(c.soc()),
                    Nullables.firstNonNull(c.seq(), () -> 1)));
            return container;
        }).toList());
    }

    void applyContainersUpdate(HouseBl entity, List<UpdateHouseBlCommand.ContainerCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initContainers(cmds.stream().map(c -> {
            HouseBlContainer container = HouseBlContainer.of(
                    entity, ContainerNumber.of(c.containerNo()),
                    ContainerType.fromCode(c.containerType()),
                    Nullables.firstNonNull(c.lengthFeet(), () -> 20));
            if (c.id() != null) container.assignIdentity(c.id(), null, null, null, null);
            container.updateDetails(new HouseBlContainer.Details(
                    SealNumber.of(c.sealNo1()), SealNumber.of(c.sealNo2()), SealNumber.of(c.sealNo3()),
                    SealNumber.of(c.sealNo4()), SealNumber.of(c.sealNo5()), SealNumber.of(c.sealNo6()),
                    Quantity.of(c.pkgQty()), c.pkgUnit(),
                    Weight.of(c.grossWeightKg()), Weight.of(c.netWeightKg()),
                    Volume.of(c.cbm()), Weight.of(c.vgmKg()), Boolean.TRUE.equals(c.soc()),
                    Nullables.firstNonNull(c.seq(), () -> 1)));
            return container;
        }).toList());
    }

    // ── ScheduleLegs ─────────────────────────────────────────────────

    void applyScheduleLegs(HouseBl entity, List<CreateHouseBlCommand.ScheduleLegCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initScheduleLegs(cmds.stream().map(c -> {
            HouseBlScheduleLeg leg = HouseBlScheduleLeg.create(null, c.toCode(), c.onBoardDt(), c.arrivalDt());
            leg.updateDetails(c.toCode(), c.byCarrier(), c.flightNo(), c.onBoardDt(), c.onBoardTm(), c.arrivalDt(), c.arrivalTm());
            return leg;
        }).toList());
    }

    void applyScheduleLegsUpdate(HouseBl entity, List<UpdateHouseBlCommand.ScheduleLegCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initScheduleLegs(cmds.stream().map(c -> {
            HouseBlScheduleLeg leg = HouseBlScheduleLeg.create(null, c.toCode(), c.onBoardDt(), c.arrivalDt());
            if (c.id() != null) leg.assignIdentity(c.id(), null, null, null, null);
            leg.updateDetails(c.toCode(), c.byCarrier(), c.flightNo(), c.onBoardDt(), c.onBoardTm(), c.arrivalDt(), c.arrivalTm());
            return leg;
        }).toList());
    }

    // ── TruckOrders ──────────────────────────────────────────────────

    void applyTruckOrders(HouseBl entity, List<CreateHouseBlCommand.TruckOrderCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initTruckOrders(cmds.stream().map(c -> {
            HouseBlTruckOrder o = HouseBlTruckOrder.create(null);
            o.updateDetails(new HouseBlTruckOrder.Details(
                    c.truckOrderNo(), c.pkgQty(), c.pkgUnit(),
                    Weight.of(c.grossWeightKg()), Volume.of(c.cbm()),
                    c.truckNo(), TruckType.fromLabel(c.truckType()), c.driver(), c.mobileNo(),
                    ContainerNumber.of(c.containerNo()), ContainerType.fromCode(c.containerType()),
                    SealNumber.of(c.sealNo1()), SealNumber.of(c.sealNo2()), SealNumber.of(c.sealNo3())));
            return o;
        }).toList());
    }

    void applyTruckOrdersUpdate(HouseBl entity, List<UpdateHouseBlCommand.TruckOrderCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initTruckOrders(cmds.stream().map(c -> {
            HouseBlTruckOrder o = HouseBlTruckOrder.create(null);
            if (c.id() != null) o.assignIdentity(c.id(), null, null, null, null);
            o.updateDetails(new HouseBlTruckOrder.Details(
                    c.truckOrderNo(), c.pkgQty(), c.pkgUnit(),
                    Weight.of(c.grossWeightKg()), Volume.of(c.cbm()),
                    c.truckNo(), TruckType.fromLabel(c.truckType()), c.driver(), c.mobileNo(),
                    ContainerNumber.of(c.containerNo()), ContainerType.fromCode(c.containerType()),
                    SealNumber.of(c.sealNo1()), SealNumber.of(c.sealNo2()), SealNumber.of(c.sealNo3())));
            return o;
        }).toList());
    }

    // ── AirCharges ───────────────────────────────────────────────────

    void applyAirCharges(HouseBl entity, List<CreateHouseBlCommand.AirChargeCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initAirCharges(cmds.stream().map(c -> {
            HouseBlAirCharge charge = HouseBlAirCharge.create(null);
            charge.updateDetails(new HouseBlAirCharge.Details(
                    c.freightCode(), CurrencyCode.of(c.currencyCode()), Per.fromCode(c.per()),
                    Nullables.mapOrNull(c.freightTerm(), FreightTerm::valueOf),
                    Weight.of(c.grossWeightKg()), RateClass.fromCode(c.rateClass()),
                    Weight.of(c.chargeWeightKg()), c.rate()));
            return charge;
        }).toList());
    }

    void applyAirChargesUpdate(HouseBl entity, List<UpdateHouseBlCommand.AirChargeCommand> cmds) {
        if (cmds == null || cmds.isEmpty()) return;
        entity.initAirCharges(cmds.stream().map(c -> {
            HouseBlAirCharge charge = HouseBlAirCharge.create(null);
            charge.updateDetails(new HouseBlAirCharge.Details(
                    c.freightCode(), CurrencyCode.of(c.currencyCode()), Per.fromCode(c.per()),
                    Nullables.mapOrNull(c.freightTerm(), FreightTerm::valueOf),
                    Weight.of(c.grossWeightKg()), RateClass.fromCode(c.rateClass()),
                    Weight.of(c.chargeWeightKg()), c.rate()));
            return charge;
        }).toList());
    }
}
