package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.common.exception.FmsException;
import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.SearchMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.UpdateMasterBlRequest;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailView;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PagedResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MasterBlAssembler {

    public PagedResult<MasterBlSummaryResponse> toSummaryPage(PagedResult<MasterBlSummaryResult> source) {
        return source.map(MasterBlSummaryResponse::from);
    }

    public MasterBlDetailResponse toDetail(MasterBlDetailView view) {
        return MasterBlDetailResponse.from(view);
    }

    public SearchMasterBlCommand toSearchCommand(SearchMasterBlRequest req) {
        return new SearchMasterBlCommand(
                req.bound(),
                req.mblNo(),
                req.shipperCode(),
                req.consigneeCode(),
                req.polCode(),
                req.podCode(),
                req.etdFrom(),
                req.etdTo()
        );
    }

    public CreateMasterBlCommand toCreateCommand(CreateMasterBlRequest req) {
        CreateMasterBlCommand.FreightCommand freightCmd = toCreateFreightCommand(req);
        return new CreateMasterBlCommand(
                req.jobDiv(),
                req.bound(),
                req.mblNo(),
                req.masterRefNo(),
                req.freightTerm(),
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.hsCode(),
                req.mainItemName(),
                req.settlePartnerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.shipmentType(),
                req.remark(),
                toSeaDetailCommand(req.seaDetail()),
                toAirDetailCommand(req.airDetail()),
                toDescCommand(req.desc()),
                toDimCommands(req.dims()),
                toLegCommands(req.scheduleLegs()),
                toChargeCommands(req.airCharges()),
                freightCmd
        );
    }

    public UpdateMasterBlCommand toUpdateCommand(UpdateMasterBlRequest req) {
        UpdateMasterBlCommand.FreightCommand freightCmd = toUpdateFreightCommand(req);
        return new UpdateMasterBlCommand(
                req.jobDiv(),
                req.bound(),
                req.freightTerm(),
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.hsCode(),
                req.mainItemName(),
                req.settlePartnerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.shipmentType(),
                req.remark(),
                toSeaDetailCommand(req.seaDetail()),
                toAirDetailCommandU(req.airDetail()),
                toDescCommand(req.desc()),
                toDimCommandsU(req.dims()),
                toLegCommandsU(req.scheduleLegs()),
                toChargeCommandsU(req.airCharges()),
                freightCmd
        );
    }

    // ── 중첩 record 변환 ──────────────────────────────────────────────

    private CreateMasterBlCommand.AirDetailCommand toAirDetailCommand(CreateMasterBlRequest.AirDetailRequest r) {
        if (r == null) return null;
        return new CreateMasterBlCommand.AirDetailCommand(r.airlineCode(), r.chargeWeightKg(), r.volumeWeightKg(), r.rateClass(), r.currencyCode(), r.declaredValueCarriage(), r.declaredValueCustoms(), r.insurance(), r.accountInformation(), r.securityStatus(), r.flightType(), r.issueDate(), r.issuePlace(), r.signature(), r.otherTerm(), r.handlingInfoCode(), r.handlingInfoText(), r.remark());
    }

    private UpdateMasterBlCommand.AirDetailCommand toAirDetailCommandU(UpdateMasterBlRequest.AirDetailRequest r) {
        if (r == null) return null;
        return new UpdateMasterBlCommand.AirDetailCommand(r.airlineCode(), r.chargeWeightKg(), r.volumeWeightKg(), r.rateClass(), r.currencyCode(), r.declaredValueCarriage(), r.declaredValueCustoms(), r.insurance(), r.accountInformation(), r.securityStatus(), r.flightType(), r.issueDate(), r.issuePlace(), r.signature(), r.otherTerm(), r.handlingInfoCode(), r.handlingInfoText(), r.remark());
    }

    private CreateMasterBlCommand.SeaDetailCommand toSeaDetailCommand(CreateMasterBlRequest.SeaDetailRequest r) {
        if (r == null) return null;
        return new CreateMasterBlCommand.SeaDetailCommand(r.loadType(), r.linerCode(), r.vesselCode(), r.vesselName(), r.voyageNo(), r.onboardDate(), r.vesselNationality(), r.serviceTerm(), r.blType(), r.porCode(), r.finalDestCode(), r.rton(), r.lineBkgNo(), r.issueDate());
    }

    private UpdateMasterBlCommand.SeaDetailCommand toSeaDetailCommand(UpdateMasterBlRequest.SeaDetailRequest r) {
        if (r == null) return null;
        return new UpdateMasterBlCommand.SeaDetailCommand(r.loadType(), r.linerCode(), r.vesselCode(), r.vesselName(), r.voyageNo(), r.onboardDate(), r.vesselNationality(), r.serviceTerm(), r.blType(), r.porCode(), r.finalDestCode(), r.rton(), r.lineBkgNo(), r.issueDate());
    }

    private CreateMasterBlCommand.DescCommand toDescCommand(CreateMasterBlRequest.DescRequest r) {
        if (r == null) return null;
        return new CreateMasterBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2());
    }

    private UpdateMasterBlCommand.DescCommand toDescCommand(UpdateMasterBlRequest.DescRequest r) {
        if (r == null) return null;
        return new UpdateMasterBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2());
    }

    private List<CreateMasterBlCommand.DimCommand> toDimCommands(List<CreateMasterBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateMasterBlCommand.DimCommand(r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<UpdateMasterBlCommand.DimCommand> toDimCommandsU(List<UpdateMasterBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateMasterBlCommand.DimCommand(r.id(), r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<CreateMasterBlCommand.ScheduleLegCommand> toLegCommands(List<CreateMasterBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateMasterBlCommand.ScheduleLegCommand(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<UpdateMasterBlCommand.ScheduleLegCommand> toLegCommandsU(List<UpdateMasterBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateMasterBlCommand.ScheduleLegCommand(r.id(), r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<CreateMasterBlCommand.AirChargeCommand> toChargeCommands(List<CreateMasterBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateMasterBlCommand.AirChargeCommand(r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    private List<UpdateMasterBlCommand.AirChargeCommand> toChargeCommandsU(List<UpdateMasterBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateMasterBlCommand.AirChargeCommand(r.id(), r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── Freight 변환 ──────────────────────────────────────────────────────────

    /**
     * CreateMasterBlRequest의 freight 필드 → CreateMasterBlCommand.FreightCommand 변환.
     * freight 관련 필드가 모두 null이면 null 반환 (freight 미포함 저장).
     */
    public CreateMasterBlCommand.FreightCommand toCreateFreightCommand(CreateMasterBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<CreateMasterBlCommand.FreightLineCommand> selling = toCreateFreightLineCommands(req.freightSelling());
        List<CreateMasterBlCommand.FreightLineCommand> buying = toCreateFreightLineCommands(req.freightBuying());
        List<String> freightErrors = new ArrayList<>();
        validateCreateFreightLines(selling, "Selling", freightErrors);
        validateCreateFreightLines(buying, "Buying", freightErrors);
        if (!freightErrors.isEmpty()) {
            throw FmsException.badRequest("FREIGHT_LINE_INVALID", String.join("\n", freightErrors));
        }
        return new CreateMasterBlCommand.FreightCommand(
                req.sellRateDt(), req.sellRateCurrencyCode(), parseBigDecimal(req.sellRate()),
                req.buyRateDt(), req.buyRateCurrencyCode(), parseBigDecimal(req.buyRate()),
                req.usdRateDt(), parseBigDecimal(req.usdRate()),
                selling, buying
        );
    }

    /**
     * UpdateMasterBlRequest의 freight 필드 → UpdateMasterBlCommand.FreightCommand 변환.
     */
    public UpdateMasterBlCommand.FreightCommand toUpdateFreightCommand(UpdateMasterBlRequest req) {
        if (req.freightSelling() == null && req.freightBuying() == null
                && req.sellRateDt() == null && req.buyRateDt() == null && req.usdRateDt() == null) {
            return null;
        }
        List<UpdateMasterBlCommand.FreightLineCommand> selling = toUpdateFreightLineCommands(req.freightSelling());
        List<UpdateMasterBlCommand.FreightLineCommand> buying = toUpdateFreightLineCommands(req.freightBuying());
        List<String> freightErrors = new ArrayList<>();
        validateUpdateFreightLines(selling, "Selling", freightErrors);
        validateUpdateFreightLines(buying, "Buying", freightErrors);
        if (!freightErrors.isEmpty()) {
            throw FmsException.badRequest("FREIGHT_LINE_INVALID", String.join("\n", freightErrors));
        }
        return new UpdateMasterBlCommand.FreightCommand(
                req.sellRateDt(), req.sellRateCurrencyCode(), parseBigDecimal(req.sellRate()),
                req.buyRateDt(), req.buyRateCurrencyCode(), parseBigDecimal(req.buyRate()),
                req.usdRateDt(), parseBigDecimal(req.usdRate()),
                selling, buying
        );
    }

    private List<CreateMasterBlCommand.FreightLineCommand> toCreateFreightLineCommands(
            List<CreateMasterBlRequest.FreightLineRequest> reqs) {
        if (reqs == null) return Collections.emptyList();
        return reqs.stream().map(r -> new CreateMasterBlCommand.FreightLineCommand(
                r.freightCode(), r.per(),
                parseBigDecimal(r.qty()), parseBigDecimal(r.price()),
                r.currency(), r.customerCode(), r.taxType(), r.performanceDt(),
                parseBigDecimal(r.exchangeRate()), parseBigDecimal(r.settleAmount()),
                parseBigDecimal(r.localAmount()), parseBigDecimal(r.usdExchangeRate()),
                parseBigDecimal(r.usdAmount()), parseBigDecimal(r.localTaxAmount()),
                r.financialDocType()
        )).toList();
    }

    private List<UpdateMasterBlCommand.FreightLineCommand> toUpdateFreightLineCommands(
            List<UpdateMasterBlRequest.FreightLineRequest> reqs) {
        if (reqs == null) return Collections.emptyList();
        return reqs.stream().map(r -> new UpdateMasterBlCommand.FreightLineCommand(
                r.freightCode(), r.per(),
                parseBigDecimal(r.qty()), parseBigDecimal(r.price()),
                r.currency(), r.customerCode(), r.taxType(), r.performanceDt(),
                parseBigDecimal(r.exchangeRate()), parseBigDecimal(r.settleAmount()),
                parseBigDecimal(r.localAmount()), parseBigDecimal(r.usdExchangeRate()),
                parseBigDecimal(r.usdAmount()), parseBigDecimal(r.localTaxAmount()),
                r.financialDocType()
        )).toList();
    }

    private void validateCreateFreightLines(List<CreateMasterBlCommand.FreightLineCommand> lines, String gridLabel,
                                             List<String> errors) {
        if (lines == null) return;
        for (int i = 0; i < lines.size(); i++) {
            CreateMasterBlCommand.FreightLineCommand l = lines.get(i);
            String err = validateFreightLine(gridLabel, i + 1, l.freightCode(), l.per(), l.currency(), l.customerCode(),
                    l.taxType(), l.performanceDt(), l.unitQuantity(), l.unitPrice());
            if (err != null) errors.add(err);
        }
    }

    private void validateUpdateFreightLines(List<UpdateMasterBlCommand.FreightLineCommand> lines, String gridLabel,
                                             List<String> errors) {
        if (lines == null) return;
        for (int i = 0; i < lines.size(); i++) {
            UpdateMasterBlCommand.FreightLineCommand l = lines.get(i);
            String err = validateFreightLine(gridLabel, i + 1, l.freightCode(), l.per(), l.currency(), l.customerCode(),
                    l.taxType(), l.performanceDt(), l.unitQuantity(), l.unitPrice());
            if (err != null) errors.add(err);
        }
    }

    /** 라인 필수 필드 + qty/price > 0 검증 (BE SSOT). 위반 메시지를 반환하고 위반 없으면 null을 반환한다. */
    private static String validateFreightLine(String gridLabel, int rowNo, String freightCode, String per,
                                              String currency, String customerCode, String taxType,
                                              String performanceDt, BigDecimal qty, BigDecimal price) {
        String prefix = gridLabel + " " + rowNo + "행: ";
        if (isBlank(freightCode) || isBlank(per) || isBlank(currency)
                || isBlank(customerCode) || isBlank(taxType) || isBlank(performanceDt)) {
            return prefix + MessageCode.FREIGHT_LINE_REQUIRED.message();
        }
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            return prefix + MessageCode.FREIGHT_LINE_QTY_INVALID.message();
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return prefix + MessageCode.FREIGHT_LINE_PRICE_INVALID.message();
        }
        return null;
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
