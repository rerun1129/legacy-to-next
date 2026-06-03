package com.freightos.fms.application.housebl;

import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.command.FreightLineCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * HouseBlCommand.FreightCommand → FreightInputCommand 변환 담당.
 * Application 계층 내부에서 FreightInputPort 호출 전 데이터를 조립한다.
 * Adapter 계층(HouseBlFreightAssembler)과 의존 방향을 분리하기 위해 별도 컴포넌트로 격리.
 */
@Component
public class HouseBlFreightCommandBuilder {

    private static final String SELLING = "SELLING";
    private static final String BUYING = "BUYING";

    /**
     * CreateHouseBlCommand 기반 FreightInputCommand 구성.
     * jobDiv별 linerCode: SEA→seaDetail.linerCode, AIR→airDetail.airlineCode, 그 외→root linerCode.
     */
    public FreightInputCommand buildFromCreate(
            CreateHouseBlCommand cmd,
            CreateHouseBlCommand.FreightCommand freightCmd) {
        String linerCode = resolveLinerCode(cmd.jobDiv(),
                cmd.seaDetail() != null ? cmd.seaDetail().linerCode() : null,
                cmd.airDetail() != null ? cmd.airDetail().airlineCode() : null,
                cmd.linerCode());
        List<FreightLineCommand> lines = buildLines(freightCmd.selling(), freightCmd.buying(),
                CreateHouseBlCommand.FreightLineCommand::freightCode,
                CreateHouseBlCommand.FreightLineCommand::per,
                CreateHouseBlCommand.FreightLineCommand::unitQuantity,
                CreateHouseBlCommand.FreightLineCommand::unitPrice,
                CreateHouseBlCommand.FreightLineCommand::currency,
                CreateHouseBlCommand.FreightLineCommand::customerCode,
                CreateHouseBlCommand.FreightLineCommand::taxType,
                CreateHouseBlCommand.FreightLineCommand::performanceDt);
        return new FreightInputCommand(
                cmd.actualCustomerCode(), linerCode, cmd.settlePartnerCode(),
                freightCmd.sellRateDt(), freightCmd.sellRateCurrencyCode(), freightCmd.sellRate(),
                freightCmd.buyRateDt(), freightCmd.buyRateCurrencyCode(), freightCmd.buyRate(),
                freightCmd.usdRateDt(), freightCmd.usdRate(),
                lines
        );
    }

    /**
     * UpdateHouseBlCommand 기반 FreightInputCommand 구성.
     */
    public FreightInputCommand buildFromUpdate(
            UpdateHouseBlCommand cmd,
            UpdateHouseBlCommand.FreightCommand freightCmd) {
        String linerCode = resolveLinerCode(cmd.jobDiv(),
                cmd.seaDetail() != null ? cmd.seaDetail().linerCode() : null,
                cmd.airDetail() != null ? cmd.airDetail().airlineCode() : null,
                cmd.linerCode());
        List<FreightLineCommand> lines = buildLines(freightCmd.selling(), freightCmd.buying(),
                UpdateHouseBlCommand.FreightLineCommand::freightCode,
                UpdateHouseBlCommand.FreightLineCommand::per,
                UpdateHouseBlCommand.FreightLineCommand::unitQuantity,
                UpdateHouseBlCommand.FreightLineCommand::unitPrice,
                UpdateHouseBlCommand.FreightLineCommand::currency,
                UpdateHouseBlCommand.FreightLineCommand::customerCode,
                UpdateHouseBlCommand.FreightLineCommand::taxType,
                UpdateHouseBlCommand.FreightLineCommand::performanceDt);
        return new FreightInputCommand(
                cmd.actualCustomerCode(), linerCode, cmd.settlePartnerCode(),
                freightCmd.sellRateDt(), freightCmd.sellRateCurrencyCode(), freightCmd.sellRate(),
                freightCmd.buyRateDt(), freightCmd.buyRateCurrencyCode(), freightCmd.buyRate(),
                freightCmd.usdRateDt(), freightCmd.usdRate(),
                lines
        );
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private static String resolveLinerCode(String jobDiv, String seaLiner, String airAirline, String rootLiner) {
        return switch (jobDiv) {
            case "SEA" -> seaLiner;
            case "AIR" -> airAirline;
            default    -> rootLiner;
        };
    }

    /**
     * selling/buying 목록을 FreightLineCommand 리스트로 병합.
     * Function 참조로 추상화하여 Create/Update 양쪽에 재사용.
     */
    private <T> List<FreightLineCommand> buildLines(
            List<T> selling, List<T> buying,
            java.util.function.Function<T, String> freightCodeFn,
            java.util.function.Function<T, String> perFn,
            java.util.function.Function<T, java.math.BigDecimal> qtyFn,
            java.util.function.Function<T, java.math.BigDecimal> priceFn,
            java.util.function.Function<T, String> currencyFn,
            java.util.function.Function<T, String> customerCodeFn,
            java.util.function.Function<T, String> taxTypeFn,
            java.util.function.Function<T, String> performanceDtFn) {
        List<FreightLineCommand> lines = new ArrayList<>();
        if (selling != null) {
            for (T item : selling) {
                lines.add(new FreightLineCommand(SELLING,
                        freightCodeFn.apply(item), perFn.apply(item),
                        qtyFn.apply(item), priceFn.apply(item),
                        currencyFn.apply(item), customerCodeFn.apply(item),
                        taxTypeFn.apply(item), performanceDtFn.apply(item)));
            }
        }
        if (buying != null) {
            for (T item : buying) {
                lines.add(new FreightLineCommand(BUYING,
                        freightCodeFn.apply(item), perFn.apply(item),
                        qtyFn.apply(item), priceFn.apply(item),
                        currencyFn.apply(item), customerCodeFn.apply(item),
                        taxTypeFn.apply(item), performanceDtFn.apply(item)));
            }
        }
        return lines;
    }
}
