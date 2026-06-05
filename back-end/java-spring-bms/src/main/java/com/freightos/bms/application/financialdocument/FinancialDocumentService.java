package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.command.AmendDocumentCommand;
import com.freightos.bms.application.financialdocument.command.IssueDocumentCommand;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentUseCase;
import com.freightos.bms.application.financialdocument.port.out.DocumentNumberGenerator;
import com.freightos.bms.application.financialdocument.port.out.DocumentSummary;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentPort;
import com.freightos.bms.application.financialdocument.port.out.FreightLineSnapshot;
import com.freightos.bms.application.port.out.CodeNameResolver;
import com.freightos.bms.domain.financialdocument.DocumentNo;
import com.freightos.bms.domain.financialdocument.FinancialDocument;
import com.freightos.bms.domain.financialdocument.enums.DocumentType;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.common.exception.FmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 금융 서류 유스케이스 구현체.
 * 발행(issue) 흐름: 라인 로드 → 검증 → 채번 → 합계 계산 → 서류 저장 → 라인 연결.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FinancialDocumentService implements FinancialDocumentUseCase {

    private final FinancialDocumentPort financialDocumentPort;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final CodeNameResolver codeNameResolver;

    @Override
    public IssueResult issueDocument(IssueDocumentCommand cmd) {
        // ① 선택 라인 로드
        List<FreightLineSnapshot> lines = financialDocumentPort.loadLinesByIds(cmd.lineIds());
        validateLines(cmd, lines);

        // ③ 채번 및 서류 번호 생성
        String commonDocType = lines.get(0).financialDocType();
        DocumentType documentType = DocumentType.fromName(commonDocType);
        // yymm = document_dt의 substring(2,6): yyyyMMdd → yy(2-3) + MM(4-5) → index 2..6
        String yymm = cmd.documentDt().substring(2, 6);
        int seq = documentNumberGenerator.nextSeq(documentType, yymm);
        DocumentNo documentNo = DocumentNo.of(documentType, yymm, seq);

        // ④ 합계 5종 계산 (null은 ZERO 처리)
        BigDecimal settleTotal = sumOrZero(lines, FreightLineSnapshot::settleAmount);
        BigDecimal localTotal = sumOrZero(lines, FreightLineSnapshot::localAmount);
        BigDecimal settleVat = sumOrZero(lines, FreightLineSnapshot::settleTaxAmount);
        BigDecimal localVat = sumOrZero(lines, FreightLineSnapshot::localTaxAmount);
        BigDecimal usdTotal = sumOrZero(lines, FreightLineSnapshot::usdAmount);

        // ⑤ 서류 도메인 생성 → 저장
        String commonCustomerCode = lines.get(0).customerCode();
        FinancialDocument document = FinancialDocument.issue(
            documentNo.value(),
            documentType,
            cmd.documentDt(),
            commonCustomerCode,
            settleTotal,
            localTotal,
            settleVat,
            localVat,
            usdTotal,
            cmd.performanceDt(),
            cmd.teamCode(),
            cmd.operator()
        );
        Long savedId = financialDocumentPort.saveDocument(document);

        // ⑥ 라인 연결 + performance_dt 전파
        financialDocumentPort.linkLines(cmd.lineIds(), savedId, cmd.performanceDt());

        return new IssueResult(savedId, documentNo.value());
    }

    @Override
    public void deleteDocument(Long financialDocumentId) {
        // 라인 연결 해제 후 서류 삭제. performance_dt는 유지(원복 없음).
        financialDocumentPort.unlinkLinesByDocument(financialDocumentId);
        financialDocumentPort.deleteDocument(financialDocumentId);
    }

    @Override
    public AmendResult amendDocument(AmendDocumentCommand cmd) {
        // ① 서류 존재 확인 — 없으면 CONFLICT(NOT_FOUND)
        DocumentSummary summary = financialDocumentPort.loadDocumentSummary(cmd.financialDocumentId())
            .orElseThrow(() -> FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_NOT_FOUND.name(),
                MessageCode.FINANCIAL_DOCUMENT_NOT_FOUND.message()
            ));

        // ② finalLineIds 비면 서류 전체 삭제(기존 deleteDocument 재사용)
        List<Long> finalLineIds = cmd.finalLineIds() != null ? cmd.finalLineIds() : Collections.emptyList();
        if (finalLineIds.isEmpty()) {
            deleteDocument(cmd.financialDocumentId());
            return new AmendResult(cmd.financialDocumentId(), summary.documentNo(), true);
        }

        // ③ 최종 라인 스냅샷 로드 → 검증
        List<FreightLineSnapshot> finalLines = financialDocumentPort.loadLinesByIds(finalLineIds);
        validateAmend(cmd, finalLines, summary);

        // ④ 현재 연결 라인 vs 최종 diff 계산
        List<Long> currentLineIds = financialDocumentPort.findLineIdsByDocument(cmd.financialDocumentId());
        Set<Long> finalSet = new HashSet<>(finalLineIds);
        Set<Long> currentSet = new HashSet<>(currentLineIds);

        List<Long> toRemove = currentSet.stream().filter(id -> !finalSet.contains(id)).toList();
        List<Long> toAdd = new ArrayList<>(finalLineIds);
        toAdd.removeAll(currentSet);

        // ⑤ 라인 link/unlink
        if (!toRemove.isEmpty()) {
            financialDocumentPort.unlinkLines(toRemove);
        }
        if (!toAdd.isEmpty()) {
            // 추가 라인에 서류의 기존 실적일 전파
            financialDocumentPort.linkLines(toAdd, cmd.financialDocumentId(), summary.performanceDt());
        }

        // ⑥ 합계 재계산 → UPDATE
        BigDecimal settleTotal = sumOrZero(finalLines, FreightLineSnapshot::settleAmount);
        BigDecimal localTotal = sumOrZero(finalLines, FreightLineSnapshot::localAmount);
        BigDecimal settleVat = sumOrZero(finalLines, FreightLineSnapshot::settleTaxAmount);
        BigDecimal localVat = sumOrZero(finalLines, FreightLineSnapshot::localTaxAmount);
        BigDecimal usdTotal = sumOrZero(finalLines, FreightLineSnapshot::usdAmount);
        financialDocumentPort.updateDocumentTotals(
            cmd.financialDocumentId(), settleTotal, localTotal, settleVat, localVat, usdTotal
        );

        // CREATED 상태 서류에 한해 헤더 4필드 갱신(비CREATED는 무시 — FE도 readonly로 전송 차단)
        if ("CREATED".equals(summary.documentStatus()) && cmd.documentDt() != null) {
            financialDocumentPort.updateDocumentHeader(
                cmd.financialDocumentId(),
                cmd.documentDt(), cmd.performanceDt(), cmd.teamCode(), cmd.operator()
            );
            // performanceDt 변경을 최종 연결 라인 전체에 재전파(§6.15: 문서→라인 전파값)
            financialDocumentPort.bulkUpdateLinePerformanceDt(finalLineIds, cmd.performanceDt());
        }

        return new AmendResult(cmd.financialDocumentId(), summary.documentNo(), false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialDocumentView> findDocumentsByBl(String blType, String blId) {
        List<FinancialDocumentView> rawViews = financialDocumentPort.findDocumentsByBl(blType, blId);
        if (rawViews.isEmpty()) return rawViews;

        Set<String> customerCodes = extractCustomerCodes(rawViews);
        Map<String, String> customerNames = customerCodes.isEmpty()
            ? Collections.emptyMap()
            : codeNameResolver.findCustomerNames(customerCodes);

        return rawViews.stream()
            .map(v -> resolveDocumentCustomerName(v, customerNames))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssuableLineView> findIssuableLines(String blType, String blId, String freightType) {
        return financialDocumentPort.findHeaderId(blType, blId)
            .map(headerId -> {
                List<IssuableLineView> rawLines = financialDocumentPort.findIssuableLines(headerId, freightType);
                if (rawLines.isEmpty()) return rawLines;

                Set<String> customerCodes = rawLines.stream()
                    .map(IssuableLineView::customerCode)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toSet());
                Map<String, String> customerNames = customerCodes.isEmpty()
                    ? Collections.emptyMap()
                    : codeNameResolver.findCustomerNames(customerCodes);

                return rawLines.stream()
                    .map(v -> resolveLineCustomerName(v, customerNames))
                    .toList();
            })
            .orElseGet(Collections::emptyList);
    }

    // ── 내부 검증 ──────────────────────────────────────────────────────────────

    /**
     * amend 전용 검증. validateLines와 독립 — "이미 발행 거부" 미적용(amend는 발행 라인 포함이 정상).
     * 단일 customer·docType / 동일 freightHeader / freightType 일치 +
     * 서류와의 customer·docType 일치 + 타 서류 소속 라인 차단.
     */
    private void validateAmend(AmendDocumentCommand cmd, List<FreightLineSnapshot> finalLines, DocumentSummary summary) {
        // 단일 customer_code 검증
        long distinctCustomerCount = finalLines.stream().map(FreightLineSnapshot::customerCode).distinct().count();
        if (distinctCustomerCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.message()
            );
        }

        // 단일 financial_doc_type 검증
        long distinctDocTypeCount = finalLines.stream().map(FreightLineSnapshot::financialDocType).distinct().count();
        if (distinctDocTypeCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.message()
            );
        }

        // 동일 freight_header_id 검증
        long distinctHeaderCount = finalLines.stream().map(FreightLineSnapshot::freightHeaderId).distinct().count();
        if (distinctHeaderCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                "선택된 라인이 서로 다른 B/L에 속합니다."
            );
        }

        // 모든 라인 freightType == cmd.freightType()
        boolean hasWrongFreightType = finalLines.stream().anyMatch(l -> !cmd.freightType().equals(l.freightType()));
        if (hasWrongFreightType) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.name(),
                "선택된 라인의 freightType이 요청과 일치하지 않습니다."
            );
        }

        // 추가/최종 라인 customer·docType이 서류와 일치
        String lineCustomerCode = finalLines.get(0).customerCode();
        if (!summary.customerCode().equals(lineCustomerCode)) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.message()
            );
        }

        String lineDocType = finalLines.get(0).financialDocType();
        if (!summary.financialDocType().equals(lineDocType)) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.message()
            );
        }

        // 각 라인 financialDocumentId는 null 또는 == cmd.financialDocumentId()만 허용(타 서류 소속 차단)
        boolean hasOtherDocument = finalLines.stream().anyMatch(l ->
            l.financialDocumentId() != null && !l.financialDocumentId().equals(cmd.financialDocumentId())
        );
        if (hasOtherDocument) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_OTHER_DOCUMENT.name(),
                MessageCode.FINANCIAL_DOCUMENT_LINE_OTHER_DOCUMENT.message()
            );
        }
    }

    private void validateLines(IssueDocumentCommand cmd, List<FreightLineSnapshot> lines) {
        // ② 검증: lineIds 비어있지 않음
        if (cmd.lineIds() == null || cmd.lineIds().isEmpty()) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.message()
            );
        }

        // 로드된 라인 수와 요청 수 일치 확인 (존재하지 않는 lineId 방지)
        if (lines.isEmpty()) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.message()
            );
        }

        // 이미 발행된 라인 거부
        boolean hasAlreadyIssued = lines.stream().anyMatch(l -> l.financialDocumentId() != null);
        if (hasAlreadyIssued) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_ALREADY_ISSUED.name(),
                MessageCode.FINANCIAL_DOCUMENT_LINE_ALREADY_ISSUED.message()
            );
        }

        // 단일 customer_code 검증 (§6.14)
        long distinctCustomerCount = lines.stream()
            .map(FreightLineSnapshot::customerCode)
            .distinct()
            .count();
        if (distinctCustomerCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.message()
            );
        }

        // 단일 financial_doc_type 검증 (§6.16)
        long distinctDocTypeCount = lines.stream()
            .map(FreightLineSnapshot::financialDocType)
            .distinct()
            .count();
        if (distinctDocTypeCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.message()
            );
        }

        // (방어) 동일 freight_header_id + (blType,blId) + freightType 확인
        long distinctHeaderCount = lines.stream()
            .map(FreightLineSnapshot::freightHeaderId)
            .distinct()
            .count();
        if (distinctHeaderCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                "선택된 라인이 서로 다른 B/L에 속합니다."
            );
        }

        boolean hasWrongFreightType = lines.stream()
            .anyMatch(l -> !cmd.freightType().equals(l.freightType()));
        if (hasWrongFreightType) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.name(),
                "선택된 라인의 freightType이 요청과 일치하지 않습니다."
            );
        }
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private BigDecimal sumOrZero(List<FreightLineSnapshot> lines, Function<FreightLineSnapshot, BigDecimal> extractor) {
        return lines.stream()
            .map(extractor)
            .map(v -> v != null ? v : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Set<String> extractCustomerCodes(List<FinancialDocumentView> views) {
        return views.stream()
            .map(FinancialDocumentView::customerCode)
            .filter(c -> c != null && !c.isBlank())
            .collect(Collectors.toSet());
    }

    private FinancialDocumentView resolveDocumentCustomerName(
            FinancialDocumentView v, Map<String, String> customerNames) {
        String name = v.customerCode() != null ? customerNames.getOrDefault(v.customerCode(), "") : "";
        return new FinancialDocumentView(
            v.financialDocumentId(), v.documentNo(), v.documentType(), v.documentDt(), v.status(),
            v.customerCode(), name,
            v.settleTotalAmount(), v.localTotalAmount(), v.settleTotalVat(), v.localTotalVat(), v.usdTotalAmount(),
            v.performanceDt(), v.teamCode(), v.operator(), v.groupFinancialNo()
        );
    }

    private IssuableLineView resolveLineCustomerName(
            IssuableLineView v, Map<String, String> customerNames) {
        String name = v.customerCode() != null ? customerNames.getOrDefault(v.customerCode(), "") : "";
        return new IssuableLineView(
            v.freightLineId(), v.freightType(), v.financialDocType(), v.freightCode(),
            v.customerCode(), name,
            v.currency(), v.settleAmount(), v.localAmount(), v.settleTaxAmount(), v.localTaxAmount(), v.usdAmount(),
            v.performanceDt(), v.financialDocumentId(), v.documentNo()
        );
    }
}
