package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.CreateTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.SearchTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlDetailResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlSummaryResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.UpdateTruckBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailView;
import com.freightos.fms.application.truckbl.projection.TruckBlListItem;
import com.freightos.fms.common.response.MessageCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Request DTO → Command 변환 및 Projection → Response DTO 변환 담당.
 * jobDiv는 항상 TRUCK으로 강제한다. hblNo는 toUpdateCommand에서 매핑하지 않는다.
 */
@Component
public class TruckBlAssembler {

    public PagedResult<TruckBlSummaryResponse> toSummaryPage(PagedResult<TruckBlListItem> source) {
        return source.map(TruckBlSummaryResponse::from);
    }

    public SearchTruckBlCommand toSearchCommand(SearchTruckBlRequest req) {
        return new SearchTruckBlCommand(
                req.bound(),
                req.truckBlNo(),
                req.etdFrom(), req.etdTo(),
                req.truckerCode(),
                req.partyCode(), req.portCode(),
                req.operatorCode(), req.teamCode(),
                req.dateKind(),
                req.partyKind(),
                req.portKind(),
                req.partnerKind(),
                req.partnerCode()
        );
    }

    public TruckBlDetailResponse toDetail(TruckBlDetailView view) {
        return TruckBlDetailResponse.from(view);
    }

    public CreateHouseBlCommand toCreateCommand(CreateTruckBlRequest req) {
        CreateHouseBlCommand.FreightCommand freight = toCreateFreightCommand(req);
        return new CreateHouseBlCommand(
                "TRUCK",
                req.bound(),
                req.hblNo(),
                null, // shipmentType
                null, // freightTerm
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.docPartnerCode(),
                req.docPartnerAddress(),
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
                null, // masterBlId
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                null, // mblNo
                null, // masterRefNo
                null, // workDivision
                null, // originalBlRef
                req.volumeDivisor(),
                null, // linerCode
                null, // linerName
                null, // vesselName (TRUCK: "TRUCK" 고정, HouseBlTruck.create에서 처리)
                null, // voyageNo (Non B/L 전용 일반 필드, Truck은 truckDetail.voyageNo로 전달)
                null, // finalDestCode
                null, // finalDestName
                null, // finalEta
                null, // volumeWeightKg
                null, // rton
                req.remark(),
                null, // seaDetail — TRUCK 미사용
                null, // airDetail — TRUCK 미사용
                toDescCommand(req.desc()),
                toDimCommands(req.dims()),
                null, // containers
                null, // scheduleLegs
                toTruckOrderCommands(req.truckOrders()),
                null, // airCharges
                toTruckDetailCreate(req),
                freight
        );
    }

    public UpdateHouseBlCommand toUpdateCommand(UpdateTruckBlRequest req) {
        // hblNo는 UpdateHouseBlCommand에 포함하지 않는다 (hblNo 매핑 금지 — §10 규칙)
        UpdateHouseBlCommand.FreightCommand freight = toUpdateFreightCommand(req);
        return new UpdateHouseBlCommand(
                "TRUCK",
                req.bound(),
                null, // shipmentType
                null, // freightTerm
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.docPartnerCode(),
                req.docPartnerAddress(),
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
                null, // masterBlId
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                null, // mblNo
                null, // masterRefNo
                null, // workDivision
                null, // originalBlRef
                req.volumeDivisor(),
                null, // linerCode
                null, // linerName
                null, // vesselName
                null, // voyageNo (Non B/L 전용 일반 필드, Truck은 truckDetail.voyageNo로 전달)
                null, // finalDestCode
                null, // finalDestName
                null, // finalEta
                null, // volumeWeightKg
                null, // rton
                req.remark(),
                null, // seaDetail
                null, // airDetail — TRUCK 미사용
                toDescCommandU(req.desc()),
                toDimCommandsU(req.dims()),
                null, // containers
                null, // scheduleLegs
                toTruckOrderCommandsU(req.truckOrders()),
                null, // airCharges
                toTruckDetailUpdate(req),
                freight
        );
    }

    // ── CREATE 서브 변환 ──────────────────────────────────────────────

    private List<CreateHouseBlCommand.DimCommand> toDimCommands(List<CreateTruckBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.DimCommand(
                r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private CreateHouseBlCommand.DescCommand toDescCommand(CreateTruckBlRequest.DescRequest r) {
        if (r == null) return null;
        return new CreateHouseBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2());
    }

    private List<CreateHouseBlCommand.TruckOrderCommand> toTruckOrderCommands(
            List<CreateTruckBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.TruckOrderCommand(
                r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    // ── UPDATE 서브 변환 ──────────────────────────────────────────────

    private List<UpdateHouseBlCommand.DimCommand> toDimCommandsU(List<UpdateTruckBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.DimCommand(
                r.id(), r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private UpdateHouseBlCommand.DescCommand toDescCommandU(UpdateTruckBlRequest.DescRequest r) {
        if (r == null) return null;
        return new UpdateHouseBlCommand.DescCommand(r.id(), r.marks(), r.description(), r.descClause1(), r.descClause2());
    }

    private List<UpdateHouseBlCommand.TruckOrderCommand> toTruckOrderCommandsU(
            List<UpdateTruckBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.TruckOrderCommand(
                r.id(), r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private CreateHouseBlCommand.TruckDetailCommand toTruckDetailCreate(CreateTruckBlRequest req) {
        return new CreateHouseBlCommand.TruckDetailCommand(
                req.truckerCode(), req.truckerPic(), req.chargeWeightKg(),
                req.pickupDate(), req.pickupTm(), req.etdTm(), req.etaTm(),
                req.loadType(), req.serviceTerm(), req.voyageNo());
    }

    private UpdateHouseBlCommand.TruckDetailCommand toTruckDetailUpdate(UpdateTruckBlRequest req) {
        return new UpdateHouseBlCommand.TruckDetailCommand(
                req.truckerCode(), req.truckerPic(), req.chargeWeightKg(),
                req.pickupDate(), req.pickupTm(), req.etdTm(), req.etaTm(),
                req.loadType(), req.serviceTerm(), req.voyageNo());
    }

    // ── Freight 변환 (HouseBlAssembler와 동일 규칙) ───────────────────────

    /**
     * CreateTruckBlRequest의 freight 필드 → CreateHouseBlCommand.FreightCommand 변환.
     * freight 관련 필드가 모두 null이면 null 반환 (freight 미포함 저장).
     */
    CreateHouseBlCommand.FreightCommand toCreateFreightCommand(CreateTruckBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<CreateHouseBlCommand.FreightLineCommand> selling = toCreateFreightLineCommands(req.freightSelling());
        List<CreateHouseBlCommand.FreightLineCommand> buying  = toCreateFreightLineCommands(req.freightBuying());
        validateFreightLines(selling);
        validateFreightLines(buying);
        return new CreateHouseBlCommand.FreightCommand(
                req.sellRateDt(), req.sellRateCurrencyCode(), parseBigDecimal(req.sellRate()),
                req.buyRateDt(), req.buyRateCurrencyCode(), parseBigDecimal(req.buyRate()),
                req.usdRateDt(), parseBigDecimal(req.usdRate()),
                selling, buying
        );
    }

    /**
     * UpdateTruckBlRequest의 freight 필드 → UpdateHouseBlCommand.FreightCommand 변환.
     */
    UpdateHouseBlCommand.FreightCommand toUpdateFreightCommand(UpdateTruckBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<UpdateHouseBlCommand.FreightLineCommand> selling = toUpdateFreightLineCommands(req.freightSelling());
        List<UpdateHouseBlCommand.FreightLineCommand> buying  = toUpdateFreightLineCommands(req.freightBuying());
        validateUpdateFreightLines(selling);
        validateUpdateFreightLines(buying);
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

    private void validateFreightLines(List<CreateHouseBlCommand.FreightLineCommand> lines) {
        if (lines == null) return;
        for (CreateHouseBlCommand.FreightLineCommand l : lines) {
            validateFreightLine(l.freightCode(), l.per(), l.currency(), l.customerCode(),
                    l.taxType(), l.performanceDt(), l.unitQuantity(), l.unitPrice());
        }
    }

    private void validateUpdateFreightLines(List<UpdateHouseBlCommand.FreightLineCommand> lines) {
        if (lines == null) return;
        for (UpdateHouseBlCommand.FreightLineCommand l : lines) {
            validateFreightLine(l.freightCode(), l.per(), l.currency(), l.customerCode(),
                    l.taxType(), l.performanceDt(), l.unitQuantity(), l.unitPrice());
        }
    }

    /** 라인 필수 필드 + qty/price > 0 검증 (BE SSOT). */
    private static void validateFreightLine(String freightCode, String per, String currency,
                                             String customerCode, String taxType, String performanceDt,
                                             BigDecimal qty, BigDecimal price) {
        if (isBlank(freightCode) || isBlank(per) || isBlank(currency)
                || isBlank(customerCode) || isBlank(taxType) || isBlank(performanceDt)) {
            throw FmsException.badRequest("FREIGHT_LINE_REQUIRED", MessageCode.FREIGHT_LINE_REQUIRED.message());
        }
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw FmsException.badRequest("FREIGHT_LINE_QTY_INVALID", MessageCode.FREIGHT_LINE_QTY_INVALID.message());
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw FmsException.badRequest("FREIGHT_LINE_PRICE_INVALID", MessageCode.FREIGHT_LINE_PRICE_INVALID.message());
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
