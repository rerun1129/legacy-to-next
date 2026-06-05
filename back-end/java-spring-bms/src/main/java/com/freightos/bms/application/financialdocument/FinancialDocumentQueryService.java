package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentSearchPort;
import com.freightos.bms.application.port.out.CodeNameResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 금융 서류 전역 검색·디테일 조회 전용 서비스.
 * FinancialDocumentService의 비대화를 막기 위해 분리.
 * 컨트롤러는 FinancialDocumentUseCase(단일 포트)만 알고, Service가 이 컴포넌트에 위임한다.
 * (feedback_new_endpoint_existing_usecase_webmvc 준수 — 컨트롤러 생성자 의존성 변경 없음)
 * A5 규칙 준수 — 이름 resolve는 페이지 단위 일괄 처리(N+1 방지).
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FinancialDocumentQueryService {

    private final FinancialDocumentSearchPort searchPort;
    private final CodeNameResolver codeNameResolver;

    public Page<FinancialDocumentSearchView> search(
            SearchFinancialDocumentCriteria criteria, Pageable pageable) {
        Page<FinancialDocumentSearchView> rawPage = searchPort.search(criteria, pageable);
        if (rawPage.isEmpty()) {
            return rawPage;
        }

        List<FinancialDocumentSearchView> rawContent = rawPage.getContent();

        // 코드 일괄 수집 (페이지 단위 1회 조회)
        Set<String> customerCodes = extractNonBlank(rawContent, FinancialDocumentSearchView::customerCode);
        Set<String> teamCodes = extractNonBlank(rawContent, FinancialDocumentSearchView::teamCode);
        Set<String> operators = extractNonBlank(rawContent, FinancialDocumentSearchView::operator);

        Map<String, String> customerNames = customerCodes.isEmpty() ? Collections.emptyMap() : codeNameResolver.findCustomerNames(customerCodes);
        Map<String, String> teamNames = teamCodes.isEmpty() ? Collections.emptyMap() : codeNameResolver.findTeamNames(teamCodes);
        Map<String, String> operatorNames = operators.isEmpty() ? Collections.emptyMap() : codeNameResolver.findOperatorNames(operators);

        List<FinancialDocumentSearchView> resolved = rawContent.stream()
            .map(v -> resolveSearchNames(v, customerNames, teamNames, operatorNames))
            .toList();

        return new PageImpl<>(resolved, pageable, rawPage.getTotalElements());
    }

    public List<FreightLineDetailView> findDocumentLines(Long documentId) {
        List<FreightLineDetailView> rawLines = searchPort.findLinesByDocument(documentId);
        if (rawLines.isEmpty()) {
            return rawLines;
        }

        Set<String> customerCodes = extractNonBlank(rawLines, FreightLineDetailView::customerCode);
        Set<String> freightCodes = extractNonBlank(rawLines, FreightLineDetailView::freightCode);

        Map<String, String> customerNames = customerCodes.isEmpty()
            ? Collections.emptyMap()
            : codeNameResolver.findCustomerNames(customerCodes);
        Map<String, String> freightNames = freightCodes.isEmpty()
            ? Collections.emptyMap()
            : codeNameResolver.findFreightNames(freightCodes);

        return rawLines.stream()
            .map(v -> resolveLineNames(v, customerNames, freightNames))
            .toList();
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────────────

    private FinancialDocumentSearchView resolveSearchNames(
            FinancialDocumentSearchView v,
            Map<String, String> customerNames,
            Map<String, String> teamNames,
            Map<String, String> operatorNames) {
        String customerName = v.customerCode() != null ? customerNames.getOrDefault(v.customerCode(), "") : "";
        String teamName = v.teamCode() != null ? teamNames.getOrDefault(v.teamCode(), "") : "";
        String operatorName = v.operator() != null ? operatorNames.getOrDefault(v.operator(), "") : "";

        return new FinancialDocumentSearchView(
            v.financialDocumentId(), v.documentNo(), v.documentType(), v.documentDt(),
            v.documentStatus(), v.customerCode(), customerName,
            v.settleTotalAmount(), v.localTotalAmount(), v.settleTotalVat(),
            v.localTotalVat(), v.usdTotalAmount(), v.performanceDt(),
            v.teamCode(), teamName, v.operator(), operatorName, v.groupFinancialNo(),
            v.blType(), v.blId(), v.jobDiv(), v.bound(), v.blNo(), v.etd(), v.eta()
        );
    }

    private FreightLineDetailView resolveLineNames(
            FreightLineDetailView v,
            Map<String, String> customerNames,
            Map<String, String> freightNames) {
        String customerName = v.customerCode() != null ? customerNames.getOrDefault(v.customerCode(), "") : "";
        String freightName = v.freightCode() != null ? freightNames.getOrDefault(v.freightCode(), "") : "";
        return new FreightLineDetailView(
            v.freightLineId(), v.freightHeaderId(), v.freightType(), v.financialDocType(),
            v.freightCode(), freightName,
            v.unitQuantity(), v.unitPrice(), v.per(), v.currency(), v.exchangeRate(),
            v.settleAmount(), v.localAmount(), v.settleTaxAmount(), v.localTaxAmount(),
            v.usdExchangeRate(), v.usdAmount(),
            v.customerCode(), customerName,
            v.taxType(), v.taxNo(), v.taxDt(), v.slipNo(), v.slipDt(),
            v.performanceDt(), v.financialDocumentId()
        );
    }

    private <T> Set<String> extractNonBlank(
            List<T> items, Function<T, String> extractor) {
        return items.stream()
            .map(extractor)
            .filter(s -> s != null && !s.isBlank())
            .collect(Collectors.toSet());
    }
}
