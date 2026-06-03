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
        List<FreightLineCommand> lines = buildLinesFromCreate(freightCmd.selling(), freightCmd.buying());
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
        List<FreightLineCommand> lines = buildLinesFromUpdate(freightCmd.selling(), freightCmd.buying());
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

    private static List<FreightLineCommand> buildLinesFromCreate(
            List<CreateMasterBlCommand.FreightLineCommand> selling,
            List<CreateMasterBlCommand.FreightLineCommand> buying) {
        List<FreightLineCommand> lines = new ArrayList<>();
        if (selling != null) {
            for (CreateMasterBlCommand.FreightLineCommand item : selling) {
                lines.add(new FreightLineCommand(SELLING,
                        item.freightCode(), item.per(), item.unitQuantity(), item.unitPrice(),
                        item.currency(), item.customerCode(), item.taxType(), item.performanceDt(),
                        item.exchangeRate(), item.settleAmount(), item.localAmount(),
                        item.usdExchangeRate(), item.usdAmount(), item.localTaxAmount(),
                        item.financialDocType()));
            }
        }
        if (buying != null) {
            for (CreateMasterBlCommand.FreightLineCommand item : buying) {
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
            List<UpdateMasterBlCommand.FreightLineCommand> selling,
            List<UpdateMasterBlCommand.FreightLineCommand> buying) {
        List<FreightLineCommand> lines = new ArrayList<>();
        if (selling != null) {
            for (UpdateMasterBlCommand.FreightLineCommand item : selling) {
                lines.add(new FreightLineCommand(SELLING,
                        item.freightCode(), item.per(), item.unitQuantity(), item.unitPrice(),
                        item.currency(), item.customerCode(), item.taxType(), item.performanceDt(),
                        item.exchangeRate(), item.settleAmount(), item.localAmount(),
                        item.usdExchangeRate(), item.usdAmount(), item.localTaxAmount(),
                        item.financialDocType()));
            }
        }
        if (buying != null) {
            for (UpdateMasterBlCommand.FreightLineCommand item : buying) {
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
