package com.freightos.pms.application.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.in.PmsPerformanceUseCase;
import com.freightos.pms.application.pms.port.out.PmsCargoQueryPort;
import com.freightos.pms.application.pms.port.out.PmsCodeNameResolver;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PMS 실적 집계 조회 서비스.
 * 1. basis 분기 → 원시 B/L 페이지 조회
 * 2. House B/L ID 목록으로 cargo 일괄 조회 (fan-out 없음)
 * 3. 코드 → 이름 일괄 resolve (N+1 방지)
 * 4. cargo 수치·profit 파생 후 PmsPerformanceRowView 조립
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PmsPerformanceQueryService implements PmsPerformanceUseCase {

    private final PmsPerformanceQueryPort queryPort;
    private final PmsCargoQueryPort cargoQueryPort;
    private final PmsCodeNameResolver codeNameResolver;

    @Override
    public Page<PmsPerformanceRowView> search(SearchPmsPerformanceCommand command, Pageable pageable) {
        Page<PmsRawBlRow> rawPage = fetchRawPage(command, pageable);
        if (rawPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<PmsRawBlRow> rows = rawPage.getContent();

        // House B/L ID 수집 (MASTER 행은 null)
        List<Long> houseBlIds = rows.stream()
            .map(PmsRawBlRow::houseBlId)
            .filter(id -> id != null)
            .distinct()
            .toList();

        Map<Long, PmsCargoRow> cargoByHouseBlId = houseBlIds.isEmpty()
            ? Collections.emptyMap()
            : cargoQueryPort.findCargoByHouseBlIds(houseBlIds).stream()
                .collect(Collectors.toMap(PmsCargoRow::houseBlId, Function.identity()));

        // 코드 일괄 수집 후 이름 resolve
        Map<String, String> customerNames = resolveCustomerNames(rows);
        Map<String, String> carrierNames = resolveCarrierNames(rows);
        Map<String, String> teamNames = resolveTeamNames(rows);
        Map<String, String> operatorNames = resolveOperatorNames(rows);

        List<PmsPerformanceRowView> content = rows.stream()
            .map(row -> toRowView(row, cargoByHouseBlId, customerNames, carrierNames, teamNames, operatorNames))
            .toList();

        return new PageImpl<>(content, pageable, rawPage.getTotalElements());
    }

    // ── 집계 기준 분기 ──────────────────────────────────────────────────────────

    private Page<PmsRawBlRow> fetchRawPage(SearchPmsPerformanceCommand command, Pageable pageable) {
        return switch (command.effectiveBasis()) {
            case FREIGHT_INPUT, TAX_ISSUED, SLIP_ISSUED -> queryPort.searchByFreightLine(command, pageable);
            case DOCUMENT_CREATED -> queryPort.searchByDocument(command, pageable);
        };
    }

    // ── 이름 resolve ─────────────────────────────────────────────────────────────

    private Map<String, String> resolveCustomerNames(List<PmsRawBlRow> rows) {
        Set<String> codes = rows.stream()
            .flatMap(r -> java.util.stream.Stream.of(r.actualCustomerCode(), r.settlePartnerCode()))
            .filter(s -> s != null && !s.isBlank())
            .collect(Collectors.toSet());
        return codes.isEmpty() ? Collections.emptyMap() : codeNameResolver.findCustomerNames(codes);
    }

    private Map<String, String> resolveCarrierNames(List<PmsRawBlRow> rows) {
        Set<String> codes = extractNonBlank(rows, PmsRawBlRow::linerCode);
        return codes.isEmpty() ? Collections.emptyMap() : codeNameResolver.findCarrierNames(codes);
    }

    private Map<String, String> resolveTeamNames(List<PmsRawBlRow> rows) {
        Set<String> codes = extractNonBlank(rows, PmsRawBlRow::teamCode);
        return codes.isEmpty() ? Collections.emptyMap() : codeNameResolver.findTeamNames(codes);
    }

    private Map<String, String> resolveOperatorNames(List<PmsRawBlRow> rows) {
        Set<String> codes = extractNonBlank(rows, PmsRawBlRow::operator);
        return codes.isEmpty() ? Collections.emptyMap() : codeNameResolver.findOperatorNames(codes);
    }

    private Set<String> extractNonBlank(List<PmsRawBlRow> rows, Function<PmsRawBlRow, String> extractor) {
        return rows.stream()
            .map(extractor)
            .filter(s -> s != null && !s.isBlank())
            .collect(Collectors.toSet());
    }

    // ── RowView 조립 ─────────────────────────────────────────────────────────────

    private PmsPerformanceRowView toRowView(
            PmsRawBlRow row,
            Map<Long, PmsCargoRow> cargoByHouseBlId,
            Map<String, String> customerNames,
            Map<String, String> carrierNames,
            Map<String, String> teamNames,
            Map<String, String> operatorNames) {

        PmsCargoRow cargo = row.houseBlId() != null ? cargoByHouseBlId.get(row.houseBlId()) : null;
        String jobDiv = row.jobDiv();

        String loadType = PmsCargoNumerics.deriveLoadType(jobDiv, cargo);
        BigDecimal rton = PmsCargoNumerics.deriveRton(jobDiv, cargo);
        BigDecimal chargeWeightKg = PmsCargoNumerics.deriveChargeWeightKg(jobDiv, cargo);
        BigDecimal cbm = cargo != null ? cargo.cbm() : null;
        BigDecimal grossWeightKg = cargo != null ? cargo.grossWeightKg() : null;
        Integer pkgQty = cargo != null ? cargo.pkgQty() : null;

        BigDecimal localProfit = PmsCargoNumerics.deriveProfit(
            row.invoiceLocalAmt(), row.debitLocalAmt(), row.paymentLocalAmt(), row.creditLocalAmt());
        BigDecimal usdProfit = PmsCargoNumerics.deriveProfit(
            row.invoiceUsdAmt(), row.debitUsdAmt(), row.paymentUsdAmt(), row.creditUsdAmt());

        return new PmsPerformanceRowView(
            row.blType(), row.blId(),
            row.houseBlNo(), row.masterBlNo(),
            row.teamCode(), name(teamNames, row.teamCode()),
            jobDiv, row.bound(), row.etd(), row.eta(), row.performanceDt(),
            row.actualCustomerCode(), name(customerNames, row.actualCustomerCode()),
            row.settlePartnerCode(), name(customerNames, row.settlePartnerCode()),
            row.linerCode(), name(carrierNames, row.linerCode()),
            row.polCode(), row.podCode(),
            row.salesManCode(), name(operatorNames, row.salesManCode()),
            row.incoterms(),
            loadType, pkgQty, rton, cbm, chargeWeightKg, grossWeightKg,
            nvl(row.invoiceLocalAmt()), nvl(row.debitLocalAmt()),
            nvl(row.paymentLocalAmt()), nvl(row.creditLocalAmt()), localProfit,
            nvl(row.invoiceUsdAmt()), nvl(row.debitUsdAmt()),
            nvl(row.paymentUsdAmt()), nvl(row.creditUsdAmt()), usdProfit,
            null, null  // col 35/36 — 추후 기능
        );
    }

    private String name(Map<String, String> map, String code) {
        if (code == null || code.isBlank()) return "";
        return map.getOrDefault(code, "");
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
