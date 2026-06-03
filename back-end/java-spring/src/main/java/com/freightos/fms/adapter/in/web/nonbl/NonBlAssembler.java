package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.CreateNonBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlDetailResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlSummaryResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.SearchNonBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.UpdateNonBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.projection.NonBlDetailView;
import com.freightos.fms.application.nonbl.projection.NonBlListItem;
import com.freightos.fms.common.response.MessageCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Request DTO → Command 변환 및 Projection → Response DTO 변환 담당.
 * Non B/L 엔드포인트는 shipmentType·freightTerm 필드가 없으므로 null로 고정하고,
 * jobDiv는 항상 NON_BL로 강제한다.
 */
@Component
public class NonBlAssembler {

    public PagedResult<NonBlSummaryResponse> toSummaryPage(PagedResult<NonBlListItem> source) {
        return source.map(NonBlSummaryResponse::from);
    }

    public NonBlDetailResponse toDetail(NonBlDetailView view) {
        return NonBlDetailResponse.from(view);
    }

    public SearchNonBlCommand toSearchCommand(SearchNonBlRequest req) {
        return new SearchNonBlCommand(
                req.bound(),
                req.hblNo(),
                req.etdFrom(), req.etdTo(),
                req.linerCode(),
                req.partyCode(), req.portCode(),
                req.vessel(), req.voyage(),
                req.operatorCode(), req.teamCode(),
                req.dateKind(),
                req.partyKind(),
                req.portKind()
        );
    }

    public CreateHouseBlCommand toCreateCommand(CreateNonBlRequest req) {
        CreateHouseBlCommand.FreightCommand freight = toCreateFreightCommand(req);
        return new CreateHouseBlCommand(
                "NON_BL",
                req.bound(),
                req.hblNo(),
                null,
                null,
                req.shipperCode(),
                null,
                req.consigneeCode(),
                null,
                req.notifyCode(),
                null,
                null,
                null,
                req.settlePartnerCode(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.actualCustomerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.salesManCode(),
                null,
                null,
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                null,
                null,
                req.workDivision(),
                req.originalBlRef(),
                req.volumeDivisor(),
                req.linerCode(),
                req.linerName(),
                req.vesselName(),
                req.voyageNo(),
                req.finalDestCode(),
                req.finalDestName(),
                req.finalEta(),
                req.volumeWeightKg(),
                req.rton(),
                req.remark(),
                toSeaDetailCommand(req.seaDetail()),
                null, // airDetail — NON_BL 미사용
                null,
                toDimCommands(req.dims()),
                toContainerCommands(req.containers()),
                toScheduleLegCommands(req.scheduleLegs()),
                toTruckOrderCommands(req.truckOrders()),
                toAirChargeCommands(req.airCharges()),
                null, // truckDetail — NON_BL 전용 아님
                freight
        );
    }

    public UpdateHouseBlCommand toUpdateCommand(UpdateNonBlRequest req) {
        UpdateHouseBlCommand.FreightCommand freight = toUpdateFreightCommand(req);
        return new UpdateHouseBlCommand(
                "NON_BL",
                req.bound(),
                null,
                null,
                req.shipperCode(),
                null,
                req.consigneeCode(),
                null,
                req.notifyCode(),
                null,
                null,
                null,
                req.settlePartnerCode(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.actualCustomerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.salesManCode(),
                null,
                null,
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                null,
                null,
                req.workDivision(),
                req.originalBlRef(),
                req.volumeDivisor(),
                req.linerCode(),
                req.linerName(),
                req.vesselName(),
                req.voyageNo(),
                req.finalDestCode(),
                req.finalDestName(),
                req.finalEta(),
                req.volumeWeightKg(),
                req.rton(),
                req.remark(),
                toSeaDetailCommandU(req.seaDetail()),
                null, // airDetail — NON_BL 미사용
                null,
                toDimCommandsU(req.dims()),
                toContainerCommandsU(req.containers()),
                toScheduleLegCommandsU(req.scheduleLegs()),
                toTruckOrderCommandsU(req.truckOrders()),
                toAirChargeCommandsU(req.airCharges()),
                null, // truckDetail — NON_BL 전용 아님
                freight
        );
    }

    // ── CREATE sub ────────────────────────────────────────────────────

    private CreateHouseBlCommand.SeaDetailCommand toSeaDetailCommand(CreateHouseBlRequest.SeaDetailRequest s) {
        if (s == null) return null;
        return new CreateHouseBlCommand.SeaDetailCommand(
                s.loadType(), s.linerCode(), s.vesselCode(), s.vesselName(), s.voyageNo(),
                s.onboardDate(), s.porCode(), s.finalDestCode(), s.issueDate(), s.noOfBl(),
                s.issuePlace(), s.doDate(), s.payableAt(), s.triangle(), s.serviceTerm(),
                s.vesselCode2(), s.vesselNationality(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages(), s.blType(), s.deliveryCode());
    }

    private List<CreateHouseBlCommand.DimCommand> toDimCommands(List<CreateHouseBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.DimCommand(
                r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<CreateHouseBlCommand.ContainerCommand> toContainerCommands(List<CreateHouseBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.ContainerCommand(
                r.containerNo(), r.containerType(), r.lengthFeet(),
                r.sealNo1(), r.sealNo2(), r.sealNo3(), r.sealNo4(), r.sealNo5(), r.sealNo6(),
                r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.netWeightKg(), r.cbm(),
                r.vgmKg(), r.soc(), r.seq())).toList();
    }

    private List<CreateHouseBlCommand.ScheduleLegCommand> toScheduleLegCommands(List<CreateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.ScheduleLegCommand(
                r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<CreateHouseBlCommand.TruckOrderCommand> toTruckOrderCommands(List<CreateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.TruckOrderCommand(
                r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private List<CreateHouseBlCommand.AirChargeCommand> toAirChargeCommands(List<CreateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.AirChargeCommand(
                r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(),
                r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── UPDATE sub ────────────────────────────────────────────────────
    // UpdateNonBlRequest reuses CreateHouseBlRequest nested records, so conversion
    // targets UpdateHouseBlCommand nested types using the same source fields.

    private UpdateHouseBlCommand.SeaDetailCommand toSeaDetailCommandU(CreateHouseBlRequest.SeaDetailRequest s) {
        if (s == null) return null;
        return new UpdateHouseBlCommand.SeaDetailCommand(
                s.loadType(), s.linerCode(), s.vesselCode(), s.vesselName(), s.voyageNo(),
                s.onboardDate(), s.porCode(), s.finalDestCode(), s.issueDate(), s.noOfBl(),
                s.issuePlace(), s.doDate(), s.payableAt(), s.triangle(), s.serviceTerm(),
                s.vesselNationality(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages(), s.blType(), s.deliveryCode());
    }

    private List<UpdateHouseBlCommand.DimCommand> toDimCommandsU(List<UpdateNonBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.DimCommand(
                r.id(), r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<UpdateHouseBlCommand.ContainerCommand> toContainerCommandsU(List<UpdateNonBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return null;
        // NonBl form은 lengthFeet/sealNo4-6/netWeightKg/vgmKg/soc/seq를 사용하지 않으므로 null 전달.
        // HouseBlNonBlJpaEntity.copyContainerFields에서 해당 필드 set을 생략하여 DB 기존 값을 유지.
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ContainerCommand(
                r.id(), r.containerNo(), r.containerType(), null,
                r.sealNo1(), r.sealNo2(), r.sealNo3(), null, null, null,
                r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), null, r.cbm(),
                null, null, null)).toList();
    }

    private List<UpdateHouseBlCommand.ScheduleLegCommand> toScheduleLegCommandsU(List<CreateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        // NonBl UPDATE는 scheduleLegs를 CreateHouseBlRequest.ScheduleLegRequest(id 없음)로 재사용 — sync 방식이므로 id null 전달
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ScheduleLegCommand(
                null, r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<UpdateHouseBlCommand.TruckOrderCommand> toTruckOrderCommandsU(List<CreateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        // CreateHouseBlRequest.TruckOrderRequest(NonBl UPDATE 재사용)에 id 필드가 없으므로 null 전달 — NonBl UPDATE도 sync 방식
        return reqs.stream().map(r -> new UpdateHouseBlCommand.TruckOrderCommand(
                null, r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private List<UpdateHouseBlCommand.AirChargeCommand> toAirChargeCommandsU(List<CreateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.AirChargeCommand(
                null, r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(),
                r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── Freight 변환 (HouseBlAssembler와 동일 규칙) ───────────────────────

    /**
     * CreateNonBlRequest의 freight 필드 → CreateHouseBlCommand.FreightCommand 변환.
     * freight 관련 필드가 모두 null이면 null 반환 (freight 미포함 저장).
     */
    CreateHouseBlCommand.FreightCommand toCreateFreightCommand(CreateNonBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<CreateHouseBlCommand.FreightLineCommand> selling = toCreateFreightLineCommands(req.freightSelling());
        List<CreateHouseBlCommand.FreightLineCommand> buying  = toCreateFreightLineCommands(req.freightBuying());
        validateCreateFreightLines(selling, "Selling");
        validateCreateFreightLines(buying, "Buying");
        return new CreateHouseBlCommand.FreightCommand(
                req.sellRateDt(), req.sellRateCurrencyCode(), parseBigDecimal(req.sellRate()),
                req.buyRateDt(), req.buyRateCurrencyCode(), parseBigDecimal(req.buyRate()),
                req.usdRateDt(), parseBigDecimal(req.usdRate()),
                selling, buying
        );
    }

    /**
     * UpdateNonBlRequest의 freight 필드 → UpdateHouseBlCommand.FreightCommand 변환.
     */
    UpdateHouseBlCommand.FreightCommand toUpdateFreightCommand(UpdateNonBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<UpdateHouseBlCommand.FreightLineCommand> selling = toUpdateFreightLineCommands(req.freightSelling());
        List<UpdateHouseBlCommand.FreightLineCommand> buying  = toUpdateFreightLineCommands(req.freightBuying());
        validateUpdateFreightLines(selling, "Selling");
        validateUpdateFreightLines(buying, "Buying");
        return new UpdateHouseBlCommand.FreightCommand(
                req.sellRateDt(), req.sellRateCurrencyCode(), parseBigDecimal(req.sellRate()),
                req.buyRateDt(), req.buyRateCurrencyCode(), parseBigDecimal(req.buyRate()),
                req.usdRateDt(), parseBigDecimal(req.usdRate()),
                selling, buying
        );
    }

    private List<CreateHouseBlCommand.FreightLineCommand> toCreateFreightLineCommands(
            List<CreateHouseBlRequest.FreightLineRequest> reqs) {
        if (reqs == null) return Collections.emptyList();
        return reqs.stream().map(r -> new CreateHouseBlCommand.FreightLineCommand(
                r.freightCode(), r.per(),
                parseBigDecimal(r.qty()), parseBigDecimal(r.price()),
                r.currency(), r.customerCode(), r.taxType(), r.performanceDt(),
                parseBigDecimal(r.exchangeRate()), parseBigDecimal(r.settleAmount()),
                parseBigDecimal(r.localAmount()), parseBigDecimal(r.usdExchangeRate()),
                parseBigDecimal(r.usdAmount()), parseBigDecimal(r.localTaxAmount()),
                r.financialDocType()
        )).toList();
    }

    private List<UpdateHouseBlCommand.FreightLineCommand> toUpdateFreightLineCommands(
            List<CreateHouseBlRequest.FreightLineRequest> reqs) {
        if (reqs == null) return Collections.emptyList();
        return reqs.stream().map(r -> new UpdateHouseBlCommand.FreightLineCommand(
                r.freightCode(), r.per(),
                parseBigDecimal(r.qty()), parseBigDecimal(r.price()),
                r.currency(), r.customerCode(), r.taxType(), r.performanceDt(),
                parseBigDecimal(r.exchangeRate()), parseBigDecimal(r.settleAmount()),
                parseBigDecimal(r.localAmount()), parseBigDecimal(r.usdExchangeRate()),
                parseBigDecimal(r.usdAmount()), parseBigDecimal(r.localTaxAmount()),
                r.financialDocType()
        )).toList();
    }

    private void validateCreateFreightLines(List<CreateHouseBlCommand.FreightLineCommand> lines, String gridLabel) {
        if (lines == null) return;
        for (int i = 0; i < lines.size(); i++) {
            CreateHouseBlCommand.FreightLineCommand l = lines.get(i);
            validateFreightLine(gridLabel, i + 1, l.freightCode(), l.per(), l.currency(), l.customerCode(),
                    l.taxType(), l.performanceDt(), l.unitQuantity(), l.unitPrice());
        }
    }

    private void validateUpdateFreightLines(List<UpdateHouseBlCommand.FreightLineCommand> lines, String gridLabel) {
        if (lines == null) return;
        for (int i = 0; i < lines.size(); i++) {
            UpdateHouseBlCommand.FreightLineCommand l = lines.get(i);
            validateFreightLine(gridLabel, i + 1, l.freightCode(), l.per(), l.currency(), l.customerCode(),
                    l.taxType(), l.performanceDt(), l.unitQuantity(), l.unitPrice());
        }
    }

    /** 라인 필수 필드 + qty/price > 0 검증 (BE SSOT). gridLabel·rowNo를 에러 메시지에 prepend한다. */
    private static void validateFreightLine(String gridLabel, int rowNo, String freightCode, String per,
                                             String currency, String customerCode, String taxType,
                                             String performanceDt, BigDecimal qty, BigDecimal price) {
        String prefix = gridLabel + " " + rowNo + "행: ";
        if (isBlank(freightCode) || isBlank(per) || isBlank(currency)
                || isBlank(customerCode) || isBlank(taxType) || isBlank(performanceDt)) {
            throw FmsException.badRequest("FREIGHT_LINE_REQUIRED", prefix + MessageCode.FREIGHT_LINE_REQUIRED.message());
        }
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw FmsException.badRequest("FREIGHT_LINE_QTY_INVALID", prefix + MessageCode.FREIGHT_LINE_QTY_INVALID.message());
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw FmsException.badRequest("FREIGHT_LINE_PRICE_INVALID", prefix + MessageCode.FREIGHT_LINE_PRICE_INVALID.message());
        }
    }

    private static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw FmsException.badRequest("FREIGHT_NUMBER_FORMAT", "운임 수치 필드 형식이 잘못되었습니다: " + value);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
