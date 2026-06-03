package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.common.exception.FmsException;
import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.SearchHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailView;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import com.freightos.fms.common.response.MessageCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Request DTO → Command 변환 및 도메인 Entity → Response DTO 변환 담당.
 * 도메인 VO/Entity 생성 책임은 Application 계층(HouseBlFactory)에 있으므로
 * 본 클래스는 1:1 필드 복사만 수행한다.
 */
@Component
public class HouseBlAssembler {

    public PagedResult<HouseBlSummaryResponse> toSummaryPage(PagedResult<HouseBlSummary> source) {
        return source.map(HouseBlSummaryResponse::from);
    }

    public HouseBlDetailResponse toDetail(HouseBlDetailView view) {
        return HouseBlDetailResponse.from(view);
    }

    public SearchHouseBlCommand toSearchCommand(SearchHouseBlRequest req) {
        return new SearchHouseBlCommand(
                req.jobDiv(),
                req.bound(),
                req.hblNo(), req.mblNo(),
                req.shipperCode(), req.consigneeCode(),
                req.polCode(), req.podCode(),
                req.etdFrom(), req.etdTo(),
                req.vessel(), req.voyage(),
                req.linerCode(), req.operatorCode(),
                req.teamCode(), req.partyCode(), req.portCode(),
                req.dateKind(),
                req.partyKind(),
                req.portKind()
        );
    }

    /** CREATE 요청 DTO를 커맨드로 변환한다. VO 변환은 없으며 1:1 필드 복사만 수행한다. */
    public CreateHouseBlCommand toCreateCommand(CreateHouseBlRequest req) {
        return toCreateCommand(req, null);
    }

    /**
     * CREATE 요청 DTO를 커맨드로 변환한다. freight 커맨드를 함께 포함한다.
     * freight가 null이면 freight 미포함 커맨드를 생성한다.
     */
    public CreateHouseBlCommand toCreateCommand(CreateHouseBlRequest req, CreateHouseBlCommand.FreightCommand freight) {
        return new CreateHouseBlCommand(
                req.jobDiv(),
                req.bound(),
                req.hblNo(),
                req.shipmentType(),
                req.freightTerm(),
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
                req.masterBlId(),
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                req.mblNo(),
                req.masterRefNo(),
                req.workDivision(),
                req.originalBlRef(),
                null,
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
                toAirDetailCommand(req.airDetail()),
                toDescCommand(req.desc()),
                toDimCommands(req.dims()),
                toContainerCommands(req.containers()),
                toScheduleLegCommands(req.scheduleLegs()),
                toTruckOrderCommands(req.truckOrders()),
                toAirChargeCommands(req.airCharges()),
                null, // truckDetail — HouseBlController는 TRUCK 전용 아님
                freight
        );
    }

    /** UPDATE 요청 DTO를 커맨드로 변환한다. VO 변환은 없으며 1:1 필드 복사만 수행한다. */
    public UpdateHouseBlCommand toUpdateCommand(UpdateHouseBlRequest req) {
        return toUpdateCommand(req, null);
    }

    /**
     * UPDATE 요청 DTO를 커맨드로 변환한다. freight 커맨드를 함께 포함한다.
     * freight가 null이면 freight 미포함 커맨드를 생성한다.
     */
    public UpdateHouseBlCommand toUpdateCommand(UpdateHouseBlRequest req, UpdateHouseBlCommand.FreightCommand freight) {
        return new UpdateHouseBlCommand(
                req.jobDiv(),
                req.bound(),
                req.shipmentType(),
                req.freightTerm(),
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
                req.masterBlId(),
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                req.mblNo(),
                req.masterRefNo(),
                req.workDivision(),
                req.originalBlRef(),
                null,
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
                toAirDetailCommandU(req.airDetail()),
                toDescCommandU(req.desc()),
                toDimCommandsU(req.dims()),
                toContainerCommandsU(req.containers()),
                toScheduleLegCommandsU(req.scheduleLegs()),
                toTruckOrderCommandsU(req.truckOrders()),
                toAirChargeCommandsU(req.airCharges()),
                null, // truckDetail — HouseBlController는 TRUCK 전용 아님
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

    private CreateHouseBlCommand.AirDetailCommand toAirDetailCommand(CreateHouseBlRequest.AirDetailRequest a) {
        if (a == null) return null;
        return new CreateHouseBlCommand.AirDetailCommand(
                a.airlineCode(), a.chargeWeightKg(), a.volumeWeightKg(),
                a.rateClass(), a.currencyCode(),
                a.declaredValueCarriage(), a.declaredValueCustoms(),
                a.insurance(), a.accountInformation(), a.otherTerm(),
                a.issueDate(), a.issuePlace(), a.signature(),
                a.fhd(), a.handlingInformationCode(), a.handlingInformationDesc(),
                a.originOfGoods(), a.cargoType());
    }

    private CreateHouseBlCommand.DescCommand toDescCommand(CreateHouseBlRequest.DescRequest r) {
        if (r == null) return null;
        return new CreateHouseBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2());
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

    private UpdateHouseBlCommand.SeaDetailCommand toSeaDetailCommandU(UpdateHouseBlRequest.SeaDetailRequest s) {
        if (s == null) return null;
        return new UpdateHouseBlCommand.SeaDetailCommand(
                s.loadType(), s.linerCode(), s.vesselCode(), s.vesselName(), s.voyageNo(),
                s.onboardDate(), s.porCode(), s.finalDestCode(), s.issueDate(), s.noOfBl(),
                s.issuePlace(), s.doDate(), s.payableAt(), s.triangle(), s.serviceTerm(),
                s.vesselNationality(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages(), s.blType(), s.deliveryCode());
    }

    private UpdateHouseBlCommand.AirDetailCommand toAirDetailCommandU(UpdateHouseBlRequest.AirDetailRequest a) {
        if (a == null) return null;
        return new UpdateHouseBlCommand.AirDetailCommand(
                a.airlineCode(), a.chargeWeightKg(), a.volumeWeightKg(),
                a.rateClass(), a.currencyCode(),
                a.declaredValueCarriage(), a.declaredValueCustoms(),
                a.insurance(), a.accountInformation(), a.otherTerm(),
                a.issueDate(), a.issuePlace(), a.signature(),
                a.fhd(), a.handlingInformationCode(), a.handlingInformationDesc(),
                a.originOfGoods(), a.cargoType());
    }

    private UpdateHouseBlCommand.DescCommand toDescCommandU(UpdateHouseBlRequest.DescRequest r) {
        if (r == null) return null;
        // Sea/Air/Truck UPDATE는 sync 방식이므로 id null 허용
        return new UpdateHouseBlCommand.DescCommand(null, r.marks(), r.description(), r.descClause1(), r.descClause2());
    }

    private List<UpdateHouseBlCommand.DimCommand> toDimCommandsU(List<UpdateHouseBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.DimCommand(
                r.id(), r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<UpdateHouseBlCommand.ContainerCommand> toContainerCommandsU(List<UpdateHouseBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ContainerCommand(
                r.id(), r.containerNo(), r.containerType(), r.lengthFeet(),
                r.sealNo1(), r.sealNo2(), r.sealNo3(), r.sealNo4(), r.sealNo5(), r.sealNo6(),
                r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.netWeightKg(), r.cbm(),
                r.vgmKg(), r.soc(), r.seq())).toList();
    }

    private List<UpdateHouseBlCommand.ScheduleLegCommand> toScheduleLegCommandsU(List<UpdateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ScheduleLegCommand(
                r.id(), r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<UpdateHouseBlCommand.TruckOrderCommand> toTruckOrderCommandsU(List<UpdateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        // TruckOrderRequest에 id 필드가 없으므로 null 전달 — HouseBl UPDATE는 sync(전체 교체) 방식
        return reqs.stream().map(r -> new UpdateHouseBlCommand.TruckOrderCommand(
                null, r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private List<UpdateHouseBlCommand.AirChargeCommand> toAirChargeCommandsU(List<UpdateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.AirChargeCommand(
                r.id(), r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(),
                r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── Freight 변환 ──────────────────────────────────────────────────────────

    /**
     * CreateHouseBlRequest의 freight 필드 → CreateHouseBlCommand.FreightCommand 변환.
     * freight 관련 필드가 모두 null이면 null 반환 (freight 미포함 저장).
     */
    public CreateHouseBlCommand.FreightCommand toCreateFreightCommand(CreateHouseBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<CreateHouseBlCommand.FreightLineCommand> selling = toCreateFreightLineCommands(req.freightSelling());
        List<CreateHouseBlCommand.FreightLineCommand> buying = toCreateFreightLineCommands(req.freightBuying());
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
     * UpdateHouseBlRequest의 freight 필드 → UpdateHouseBlCommand.FreightCommand 변환.
     */
    public UpdateHouseBlCommand.FreightCommand toUpdateFreightCommand(UpdateHouseBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<UpdateHouseBlCommand.FreightLineCommand> selling = toUpdateFreightLineCommands(req.freightSelling());
        List<UpdateHouseBlCommand.FreightLineCommand> buying = toUpdateFreightLineCommands(req.freightBuying());
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
            List<UpdateHouseBlRequest.FreightLineRequest> reqs) {
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
