package com.freightos.pms.application.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.in.PmsPerformanceUseCase;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import com.freightos.pms.application.pms.projection.PmsCargoRow;
import com.freightos.pms.application.pms.projection.PmsPerformanceRowView;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * PMS 실적 집계 조회 서비스.
 *
 * 단일 쿼리 구조 (page CTE + identity/cargo/name LEFT JOIN):
 *   - identity (HOUSE: hblNo/mblNo/jobDiv 등, MASTER: mblNo/jobDiv 등)
 *   - cargo 수치 (pkgQty/cbm/grossWeightKg + 확장 테이블)
 *   - 이름 (accName/spcName/lcName/teamName/salesManName)
 * 이 모든 정보가 한 쿼리에서 반환되므로 Phase-2 keyed lookup과 Phase-4 code→name 해소가 불필요하다.
 *
 * 서비스는 PmsCargoNumerics(순수 계산)와 36컬럼 RowView 조립만 담당한다.
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PmsPerformanceQueryService implements PmsPerformanceUseCase {

    private final PmsPerformanceQueryPort queryPort;

    @Override
    public Page<PmsPerformanceRowView> search(SearchPmsPerformanceCommand command, Pageable pageable) {
        Page<PmsRawBlRow> rawPage = fetchRawPage(command, pageable);
        if (rawPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<PmsPerformanceRowView> content = rawPage.getContent().stream()
            .map(this::toRowView)
            .toList();

        return new PageImpl<>(content, pageable, rawPage.getTotalElements());
    }

    // ── basis 분기 ────────────────────────────────────────────────────────────

    private Page<PmsRawBlRow> fetchRawPage(SearchPmsPerformanceCommand command, Pageable pageable) {
        return switch (command.effectiveBasis()) {
            case FREIGHT_INPUT, TAX_ISSUED, SLIP_ISSUED -> queryPort.searchByFreightLine(command, pageable);
            case DOCUMENT_CREATED -> queryPort.searchByDocument(command, pageable);
        };
    }

    // ── RowView 조립 ──────────────────────────────────────────────────────────

    /**
     * 단일 쿼리에서 반환된 PmsRawBlRow → 36컬럼 PmsPerformanceRowView.
     * cargo 수치 파생은 PmsCargoNumerics(순수 계산)에 위임.
     * 이름은 쿼리 결과에서 직접 읽음 (code→name 추가 조회 없음).
     */
    private PmsPerformanceRowView toRowView(PmsRawBlRow row) {
        // 단일 쿼리에서 반환된 cargo 필드로 PmsCargoRow를 재구성하여 PmsCargoNumerics 재사용
        PmsCargoRow cargo = row.houseBlId() != null ? buildCargoRow(row) : null;
        String jobDiv = row.jobDiv();

        BigDecimal localProfit = PmsCargoNumerics.deriveProfit(
            row.invoiceLocalAmt(), row.debitLocalAmt(), row.paymentLocalAmt(), row.creditLocalAmt());
        BigDecimal usdProfit = PmsCargoNumerics.deriveProfit(
            row.invoiceUsdAmt(), row.debitUsdAmt(), row.paymentUsdAmt(), row.creditUsdAmt());

        return new PmsPerformanceRowView(
            row.blType(), row.blId(),
            row.houseBlNo(), row.masterBlNo(),
            row.teamCode(), blank(row.teamName()),
            jobDiv, row.bound(), row.etd(), row.eta(), row.performanceDt(),
            row.actualCustomerCode(), blank(row.accName()),
            row.settlePartnerCode(), blank(row.spcName()),
            row.linerCode(), blank(row.lcName()),
            row.polCode(), row.podCode(),
            row.salesManCode(), blank(row.salesManName()),
            row.incoterms(),
            PmsCargoNumerics.deriveLoadType(jobDiv, cargo),
            cargo != null ? cargo.pkgQty() : null,
            PmsCargoNumerics.deriveRton(jobDiv, cargo),
            cargo != null ? cargo.cbm() : null,
            PmsCargoNumerics.deriveChargeWeightKg(jobDiv, cargo),
            cargo != null ? cargo.grossWeightKg() : null,
            nvl(row.invoiceLocalAmt()), nvl(row.debitLocalAmt()),
            nvl(row.paymentLocalAmt()), nvl(row.creditLocalAmt()), localProfit,
            nvl(row.invoiceUsdAmt()), nvl(row.debitUsdAmt()),
            nvl(row.paymentUsdAmt()), nvl(row.creditUsdAmt()), usdProfit,
            null, null  // col 35/36 — 추후 기능
        );
    }

    /**
     * 단일 쿼리 결과 행에서 PmsCargoRow를 재구성한다 (HOUSE 행 전용).
     * PmsCargoNumerics가 PmsCargoRow 타입을 요구하므로 얇은 래퍼로 변환.
     */
    private PmsCargoRow buildCargoRow(PmsRawBlRow row) {
        return new PmsCargoRow(
            row.houseBlId(),
            row.pkgQty(),
            row.cbm(),
            row.grossWeightKg(),
            row.seaLoadType(),
            row.airChargeWeightKg(),
            row.truckChargeWeightKg(),
            row.truckLoadType(),
            row.nonBlRton()
        );
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private String blank(String s) {
        return s != null ? s : "";
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
