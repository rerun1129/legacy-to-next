package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.exception.FmsException;
import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.CreateNonBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.UpdateNonBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.common.response.MessageCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Non B/L freight 관련 Request DTO → Command 변환 전담.
 * NonBlAssembler 500줄 한계 방지를 위해 freight 블록만 분리.
 */
@Component
public class NonBlFreightAssembler {

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
        List<String> freightErrors = new ArrayList<>();
        validateCreateFreightLines(selling, "Selling", freightErrors);
        validateCreateFreightLines(buying, "Buying", freightErrors);
        if (!freightErrors.isEmpty()) {
            throw FmsException.badRequest("FREIGHT_LINE_INVALID", String.join("\n", freightErrors));
        }
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
        List<String> freightErrors = new ArrayList<>();
        validateUpdateFreightLines(selling, "Selling", freightErrors);
        validateUpdateFreightLines(buying, "Buying", freightErrors);
        if (!freightErrors.isEmpty()) {
            throw FmsException.badRequest("FREIGHT_LINE_INVALID", String.join("\n", freightErrors));
        }
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

    private void validateCreateFreightLines(List<CreateHouseBlCommand.FreightLineCommand> lines, String gridLabel,
                                             List<String> errors) {
        if (lines == null) return;
        for (int i = 0; i < lines.size(); i++) {
            CreateHouseBlCommand.FreightLineCommand l = lines.get(i);
            String err = validateFreightLine(gridLabel, i + 1, l.freightCode(), l.per(), l.currency(), l.customerCode(),
                    l.taxType(), l.performanceDt(), l.unitQuantity(), l.unitPrice());
            if (err != null) errors.add(err);
        }
    }

    private void validateUpdateFreightLines(List<UpdateHouseBlCommand.FreightLineCommand> lines, String gridLabel,
                                             List<String> errors) {
        if (lines == null) return;
        for (int i = 0; i < lines.size(); i++) {
            UpdateHouseBlCommand.FreightLineCommand l = lines.get(i);
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
