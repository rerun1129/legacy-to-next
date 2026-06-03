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
        List<FreightLineCommand> lines = buildLinesFromCreate(freightCmd.selling(), freightCmd.buying());
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
        List<FreightLineCommand> lines = buildLinesFromUpdate(freightCmd.selling(), freightCmd.buying());
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

    private static List<FreightLineCommand> buildLinesFromCreate(
            List<CreateHouseBlCommand.FreightLineCommand> selling,
            List<CreateHouseBlCommand.FreightLineCommand> buying) {
        List<FreightLineCommand> lines = new ArrayList<>();
        if (selling != null) {
            for (CreateHouseBlCommand.FreightLineCommand item : selling) {
                lines.add(new FreightLineCommand(SELLING,
                        item.freightCode(), item.per(), item.unitQuantity(), item.unitPrice(),
                        item.currency(), item.customerCode(), item.taxType(), item.performanceDt(),
                        item.exchangeRate(), item.settleAmount(), item.localAmount(),
                        item.usdExchangeRate(), item.usdAmount(), item.localTaxAmount(),
                        item.financialDocType()));
            }
        }
        if (buying != null) {
            for (CreateHouseBlCommand.FreightLineCommand item : buying) {
                lines.add(new FreightLineCommand(BUYING,
                        item.freightCode(), item.per(), item.unitQuantity(), item.unitPrice(),
                        item.currency(), item.customerCode(), item.taxType(), item.performanceDt(),
                        item.exchangeRate(), item.settleAmount(), item.localAmount(),
                        item.usdExchangeRate(), item.usdAmount(), item.localTaxAmount(),
                        item.financialDocType()));
            }
        }
        return lines;
    }

    private static List<FreightLineCommand> buildLinesFromUpdate(
            List<UpdateHouseBlCommand.FreightLineCommand> selling,
            List<UpdateHouseBlCommand.FreightLineCommand> buying) {
        List<FreightLineCommand> lines = new ArrayList<>();
        if (selling != null) {
            for (UpdateHouseBlCommand.FreightLineCommand item : selling) {
                lines.add(new FreightLineCommand(SELLING,
                        item.freightCode(), item.per(), item.unitQuantity(), item.unitPrice(),
                        item.currency(), item.customerCode(), item.taxType(), item.performanceDt(),
                        item.exchangeRate(), item.settleAmount(), item.localAmount(),
                        item.usdExchangeRate(), item.usdAmount(), item.localTaxAmount(),
                        item.financialDocType()));
            }
        }
        if (buying != null) {
            for (UpdateHouseBlCommand.FreightLineCommand item : buying) {
                lines.add(new FreightLineCommand(BUYING,
                        item.freightCode(), item.per(), item.unitQuantity(), item.unitPrice(),
                        item.currency(), item.customerCode(), item.taxType(), item.performanceDt(),
                        item.exchangeRate(), item.settleAmount(), item.localAmount(),
                        item.usdExchangeRate(), item.usdAmount(), item.localTaxAmount(),
                        item.financialDocType()));
            }
        }
        return lines;
    }
}
