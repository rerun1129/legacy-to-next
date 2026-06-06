package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.command.IssueFreightLineCommand;
import com.freightos.bms.application.financialdocument.port.in.FreightLineIssueUseCase;
import com.freightos.bms.application.financialdocument.port.out.DocumentLineFlag;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentPort;
import com.freightos.bms.application.financialdocument.port.out.FreightLineIssuePort;
import com.freightos.bms.application.financialdocument.port.out.FreightLineIssueSnapshot;
import com.freightos.bms.application.financialdocument.port.out.IssueNumberGenerator;
import com.freightos.bms.application.port.out.CodeNameResolver;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.bms.domain.financialdocument.IssueNo;
import com.freightos.bms.domain.financialdocument.enums.DocumentStatus;
import com.freightos.bms.domain.financialdocument.enums.IssueType;
import com.freightos.common.exception.FmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 운임 행 발급 유스케이스 구현체 (단계 E).
 * FinancialDocumentGroupService와 동일 패키지 내 별도 서비스로 분리(SRP).
 * 흐름: 검증 → 채번 → 라인 기록(.execute()) → 서류 상태 승급(DB 재조회 후 재파생).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FreightLineIssueService implements FreightLineIssueUseCase {

    private final FreightLineIssuePort freightLineIssuePort;
    private final FinancialDocumentPort financialDocumentPort;
    private final IssueNumberGenerator issueNumberGenerator;
    private final CodeNameResolver codeNameResolver;

    @Override
    @Transactional(readOnly = true)
    public Page<FreightLineIssueRowView> searchFreightLines(
            SearchFreightLineCriteria criteria, Pageable pageable) {
        Page<FreightLineIssueRowView> rawPage = freightLineIssuePort.searchFreightLines(criteria, pageable);
        if (rawPage.isEmpty()) {
            return rawPage;
        }

        List<FreightLineIssueRowView> rawContent = rawPage.getContent();
        Set<String> customerCodes = rawContent.stream()
            .map(FreightLineIssueRowView::customerCode)
            .filter(c -> c != null && !c.isBlank())
            .collect(Collectors.toSet());

        Map<String, String> customerNames = customerCodes.isEmpty()
            ? Collections.emptyMap()
            : codeNameResolver.findCustomerNames(customerCodes);

        List<FreightLineIssueRowView> resolved = rawContent.stream()
            .map(v -> resolveCustomerName(v, customerNames))
            .toList();

        return new PageImpl<>(resolved, pageable, rawPage.getTotalElements());
    }

    @Override
    public IssueFreightLineResult issue(IssueFreightLineCommand cmd) {
        // ① 발급 종류 변환 (Adapter → Command 경계, domain enum)
        IssueType issueType = parseIssueType(cmd.issueType());

        // ② 로드 (Tuple projection — 1차캐시 staleness 회피)
        List<Long> distinctIds = distinctIds(cmd.lineIds());
        List<FreightLineIssueSnapshot> lines = freightLineIssuePort.loadIssueLinesByIds(distinctIds);

        // ③ 검증
        validateIssue(distinctIds, lines, issueType);

        // ④ 채번 — issueDt length 가드(S2)
        String issueDt = cmd.issueDt();
        if (issueDt == null || issueDt.length() < 6) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_NOT_FOUND.name(),
                "발급일(issueDt)이 유효하지 않습니다: " + issueDt
            );
        }
        String yymm = issueDt.substring(2, 6);
        int seq = issueNumberGenerator.nextSeq(issueType, yymm);
        String issueNo = IssueNo.of(issueType, yymm, seq).value();

        // ⑤ 라인 기록 (.execute()로 즉시 반영 — bulk update 후 DB 재조회 보장)
        switch (issueType) {
            case TAX -> freightLineIssuePort.bulkUpdateLineTax(distinctIds, issueNo, issueDt);
            case SLIP -> freightLineIssuePort.bulkUpdateLineSlip(distinctIds, issueNo, issueDt);
        }

        // ⑥ 서류 상태 재파생 (DB 재조회 — S6: bulk update 후 1차캐시 무시)
        List<Long> documentIds = lines.stream()
            .map(FreightLineIssueSnapshot::financialDocumentId)
            .distinct()
            .toList();

        List<DocumentLineFlag> flags = freightLineIssuePort.loadDocumentTaxSlipFlags(documentIds);
        List<Long> affectedIds = promoteDocumentStatus(flags);

        Map<Long, String> statusByDocumentId = buildStatusMap(flags, affectedIds);

        return new IssueFreightLineResult(issueNo, affectedIds, statusByDocumentId);
    }

    // ── 검증 ──────────────────────────────────────────────────────────────────

    private void validateIssue(
            List<Long> distinctIds,
            List<FreightLineIssueSnapshot> lines,
            IssueType issueType) {

        // lineIds 비어있는지
        if (distinctIds == null || distinctIds.isEmpty()) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.message()
            );
        }

        // S1: 로드 수 == 요청 distinct 수 일치 (동시 삭제로 인한 부분채번 방지)
        if (lines.size() != distinctIds.size()) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                "요청한 운임 행의 일부를 찾을 수 없습니다. 요청=" + distinctIds.size() + ", 로드=" + lines.size()
            );
        }

        // 서류 미발행 라인 거부 (financial_document_id IS NULL — 결정1)
        boolean hasUnissued = lines.stream().anyMatch(l -> l.financialDocumentId() == null);
        if (hasUnissued) {
            throw FmsException.conflict(
                MessageCode.FREIGHT_LINE_NOT_ISSUED_YET.name(),
                MessageCode.FREIGHT_LINE_NOT_ISSUED_YET.message()
            );
        }

        // 단일 customer (S3 혼재는 기존 MessageCode 재사용)
        long distinctCustomerCount = lines.stream()
            .map(FreightLineIssueSnapshot::customerCode)
            .distinct()
            .count();
        if (distinctCustomerCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.message()
            );
        }

        // 단일 financial_doc_type
        long distinctDocTypeCount = lines.stream()
            .map(FreightLineIssueSnapshot::financialDocType)
            .distinct()
            .count();
        if (distinctDocTypeCount > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_TYPE.message()
            );
        }

        // 이미 발급된 라인 거부 (S3 신규 MessageCode — 발급 종류별 분리)
        switch (issueType) {
            case TAX -> {
                boolean hasAlreadyTaxed = lines.stream().anyMatch(l -> l.taxNo() != null);
                if (hasAlreadyTaxed) {
                    throw FmsException.conflict(
                        MessageCode.FREIGHT_LINE_TAX_ALREADY_ISSUED.name(),
                        MessageCode.FREIGHT_LINE_TAX_ALREADY_ISSUED.message()
                    );
                }
            }
            case SLIP -> {
                boolean hasAlreadySlipped = lines.stream().anyMatch(l -> l.slipNo() != null);
                if (hasAlreadySlipped) {
                    throw FmsException.conflict(
                        MessageCode.FREIGHT_LINE_SLIP_ALREADY_ISSUED.name(),
                        MessageCode.FREIGHT_LINE_SLIP_ALREADY_ISSUED.message()
                    );
                }
            }
        }
    }

    // ── 상태 승급 ─────────────────────────────────────────────────────────────

    /**
     * flags에서 서류별 파생 상태를 계산해 우선순위 미만인 서류만 승급한다.
     * 파생 규칙: hasSlip→SLIP, hasTax(only)→TAX, 둘 다 없으면 null(재파생 없음).
     * C3: DocumentStatus.fromName() null 반환 가드 복제(FinancialDocumentGroupService:179-181).
     */
    private List<Long> promoteDocumentStatus(List<DocumentLineFlag> flags) {
        Map<String, List<Long>> groupedByStatus = new HashMap<>();

        for (DocumentLineFlag flag : flags) {
            String derived = flag.hasSlip() ? DocumentStatus.SLIP.name()
                : (flag.hasTax() ? DocumentStatus.TAX.name() : null);
            if (derived == null) continue;

            DocumentStatus currentStatus = DocumentStatus.fromName(flag.currentDocumentStatus());
            DocumentStatus derivedStatus = DocumentStatus.fromName(derived);

            // C3: null status 가드
            if (currentStatus != null && derivedStatus != null
                    && derivedStatus.priority() > currentStatus.priority()) {
                groupedByStatus
                    .computeIfAbsent(derived, k -> new ArrayList<>())
                    .add(flag.financialDocumentId());
            }
        }

        List<Long> affectedIds = new ArrayList<>();
        groupedByStatus.forEach((status, ids) -> {
            financialDocumentPort.bulkUpdateDocumentStatus(ids, status);
            affectedIds.addAll(ids);
        });

        return Collections.unmodifiableList(affectedIds);
    }

    /**
     * 반환용 statusByDocumentId 맵 구성.
     * affectedIds에 포함된 서류는 재파생 상태, 나머지는 기존 상태.
     */
    private Map<Long, String> buildStatusMap(List<DocumentLineFlag> flags, List<Long> affectedIds) {
        Set<Long> affectedSet = Set.copyOf(affectedIds);
        Map<Long, String> result = new HashMap<>();

        for (DocumentLineFlag flag : flags) {
            if (affectedSet.contains(flag.financialDocumentId())) {
                String derived = flag.hasSlip() ? DocumentStatus.SLIP.name() : DocumentStatus.TAX.name();
                result.put(flag.financialDocumentId(), derived);
            } else {
                result.put(flag.financialDocumentId(), flag.currentDocumentStatus());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private IssueType parseIssueType(String issueTypeName) {
        IssueType type = IssueType.fromName(issueTypeName);
        if (type == null) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_NOT_FOUND.name(),
                "알 수 없는 발급 종류: " + issueTypeName
            );
        }
        return type;
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return ids.stream().distinct().toList();
    }

    private FreightLineIssueRowView resolveCustomerName(
            FreightLineIssueRowView v, Map<String, String> customerNames) {
        String name = v.customerCode() != null ? customerNames.getOrDefault(v.customerCode(), "") : "";
        return new FreightLineIssueRowView(
            v.freightLineId(), v.freightHeaderId(),
            v.blType(), v.blId(), v.blNo(), v.jobDiv(), v.bound(), v.etd(),
            v.freightType(), v.financialDocType(), v.freightCode(),
            v.customerCode(), name,
            v.currency(), v.settleAmount(), v.localAmount(),
            v.settleTaxAmount(), v.localTaxAmount(), v.usdAmount(),
            v.performanceDt(), v.financialDocumentId(), v.documentNo(), v.documentStatus(),
            v.taxNo(), v.taxDt(), v.slipNo(), v.slipDt()
        );
    }
}
