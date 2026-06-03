package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.command.FreightLineCommand;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MasterBlCommand.FreightCommand → FreightInputCommand 변환 담당.
 * Application 계층 내부에서 FreightInputPort 호출 전 데이터를 조립한다.
 *
 * house와 달리 master_bl 테이블에 actualCustomerCode 컬럼이 없으므로
 * shipperCode를 actualCustomerCode 역할로 사용한다.
 * linerCode: SEA→seaDetail.linerCode, AIR→airDetail.airlineCode.
 */
@Component
public class MasterBlFreightCommandBuilder {

    private static final String SELLING = "SELLING";
    private static final String BUYING = "BUYING";

    /**
     * CreateMasterBlCommand 기반 FreightInputCommand 구성.
     * jobDiv별 linerCode: SEA→seaDetail.linerCode, AIR→airDetail.airlineCode.
     */
    public FreightInputCommand buildFromCreate(
            CreateMasterBlCommand cmd,
            CreateMasterBlCommand.FreightCommand freightCmd) {
        String linerCode = resolveLinerCode(cmd.jobDiv(),
                cmd.seaDetail() != null ? cmd.seaDetail().linerCode() : null,
                cmd.airDetail() != null ? cmd.airDetail().airlineCode() : null);
        List<FreightLineCommand> lines = buildLines(freightCmd.selling(), freightCmd.buying(),
                CreateMasterBlCommand.FreightLineCommand::freightCode,
                CreateMasterBlCommand.FreightLineCommand::per,
                CreateMasterBlCommand.FreightLineCommand::unitQuantity,
                CreateMasterBlCommand.FreightLineCommand::unitPrice,
                CreateMasterBlCommand.FreightLineCommand::currency,
                CreateMasterBlCommand.FreightLineCommand::customerCode,
                CreateMasterBlCommand.FreightLineCommand::taxType,
                CreateMasterBlCommand.FreightLineCommand::performanceDt);
        return new FreightInputCommand(
                cmd.shipperCode(), linerCode, cmd.settlePartnerCode(),
                freightCmd.sellRateDt(), freightCmd.sellRateCurrencyCode(), freightCmd.sellRate(),
                freightCmd.buyRateDt(), freightCmd.buyRateCurrencyCode(), freightCmd.buyRate(),
                freightCmd.usdRateDt(), freightCmd.usdRate(),
                lines
        );
    }

    /**
     * UpdateMasterBlCommand 기반 FreightInputCommand 구성.
     */
    public FreightInputCommand buildFromUpdate(
            UpdateMasterBlCommand cmd,
            UpdateMasterBlCommand.FreightCommand freightCmd) {
        String linerCode = resolveLinerCode(cmd.jobDiv(),
                cmd.seaDetail() != null ? cmd.seaDetail().linerCode() : null,
                cmd.airDetail() != null ? cmd.airDetail().airlineCode() : null);
        List<FreightLineCommand> lines = buildLines(freightCmd.selling(), freightCmd.buying(),
                UpdateMasterBlCommand.FreightLineCommand::freightCode,
                UpdateMasterBlCommand.FreightLineCommand::per,
                UpdateMasterBlCommand.FreightLineCommand::unitQuantity,
                UpdateMasterBlCommand.FreightLineCommand::unitPrice,
                UpdateMasterBlCommand.FreightLineCommand::currency,
                UpdateMasterBlCommand.FreightLineCommand::customerCode,
                UpdateMasterBlCommand.FreightLineCommand::taxType,
                UpdateMasterBlCommand.FreightLineCommand::performanceDt);
        return new FreightInputCommand(
                cmd.shipperCode(), linerCode, cmd.settlePartnerCode(),
                freightCmd.sellRateDt(), freightCmd.sellRateCurrencyCode(), freightCmd.sellRate(),
                freightCmd.buyRateDt(), freightCmd.buyRateCurrencyCode(), freightCmd.buyRate(),
                freightCmd.usdRateDt(), freightCmd.usdRate(),
                lines
        );
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /** SEA→seaDetail.linerCode, AIR→airDetail.airlineCode, 그 외→null. */
    private static String resolveLinerCode(String jobDiv, String seaLiner, String airAirline) {
        return switch (jobDiv) {
            case "SEA" -> seaLiner;
            case "AIR" -> airAirline;
            default    -> null;
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
