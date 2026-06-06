package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.command.CancelFreightLineCommand;
import com.freightos.bms.application.financialdocument.port.out.DocumentLineFlag;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentPort;
import com.freightos.bms.application.financialdocument.port.out.FreightLineIssuePort;
import com.freightos.bms.application.financialdocument.port.out.FreightLineIssueSnapshot;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.bms.domain.financialdocument.enums.DocumentStatus;
import com.freightos.bms.domain.financialdocument.enums.IssueType;
import com.freightos.common.exception.FmsException;
import lombok.RequiredArgsConstructor;
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
 * 운임 행 발급 취소 서비스 (단계 E 역연산).
 * FreightLineIssueService에서 위임받아 SRP 분리 및 300줄 규칙 준수.
 * 흐름: 검증 → 하드 클리어(NULL) → 서류 상태 강등(DB 재조회 후 재파생).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FreightLineCancelService {

    private final FreightLineIssuePort freightLineIssuePort;
    private final FinancialDocumentPort financialDocumentPort;

    public CancelFreightLineResult cancel(CancelFreightLineCommand cmd) {
        // ① 발급 종류 변환 (parseIssueType 패턴)
        IssueType issueType = parseIssueType(cmd.issueType());

        // ② distinct lineIds 산출
        List<Long> distinctIds = distinctIds(cmd.lineIds());

        // ③ 스냅샷 로드 (taxNo/slipNo 포함)
        List<FreightLineIssueSnapshot> lines = freightLineIssuePort.loadIssueLinesByIds(distinctIds);

        // ④ 검증
        validateCancel(distinctIds, lines, issueType);

        // ⑤ 하드 클리어 (.execute() 즉시 반영 — 이후 DB 재조회 보장)
        switch (issueType) {
            case TAX -> freightLineIssuePort.bulkClearLineTax(distinctIds);
            case SLIP -> freightLineIssuePort.bulkClearLineSlip(distinctIds);
        }

        // ⑥ 서류 상태 강등 재파생 (DB 재조회 — 클리어 후 1차캐시 무시)
        List<Long> documentIds = lines.stream()
            .map(FreightLineIssueSnapshot::financialDocumentId)
            .filter(id -> id != null)
            .distinct()
            .toList();

        List<DocumentLineFlag> flags = freightLineIssuePort.loadDocumentTaxSlipFlags(documentIds);
        List<Long> affectedIds = demoteDocumentStatus(flags);

        Map<Long, String> statusByDocumentId = buildStatusMap(flags, affectedIds);

        return new CancelFreightLineResult(affectedIds, statusByDocumentId);
    }

    // ── 검증 ──────────────────────────────────────────────────────────────────

    private void validateCancel(
            List<Long> distinctIds,
            List<FreightLineIssueSnapshot> lines,
            IssueType issueType) {

        if (distinctIds == null || distinctIds.isEmpty()) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.message()
            );
        }

        // 로드 수 != 요청 distinct 수 (동시 삭제로 인한 불일치 방지)
        if (lines.size() != distinctIds.size()) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_LINE_EMPTY.name(),
                "요청한 운임 행의 일부를 찾을 수 없습니다. 요청=" + distinctIds.size() + ", 로드=" + lines.size()
            );
        }

        // 발급 여부 검증: 취소하려면 해당 번호가 있어야 함
        switch (issueType) {
            case TAX -> {
                boolean hasNotIssued = lines.stream().anyMatch(l -> l.taxNo() == null);
                if (hasNotIssued) {
                    throw FmsException.conflict(
                        MessageCode.FREIGHT_LINE_TAX_NOT_ISSUED.name(),
                        MessageCode.FREIGHT_LINE_TAX_NOT_ISSUED.message()
                    );
                }
            }
            case SLIP -> {
                boolean hasNotIssued = lines.stream().anyMatch(l -> l.slipNo() == null);
                if (hasNotIssued) {
                    throw FmsException.conflict(
                        MessageCode.FREIGHT_LINE_SLIP_NOT_ISSUED.name(),
                        MessageCode.FREIGHT_LINE_SLIP_NOT_ISSUED.message()
                    );
                }
            }
        }
    }

    // ── 상태 강등 ─────────────────────────────────────────────────────────────

    /**
     * 클리어 후 flags에서 서류별 파생 상태를 계산해 재파생 상태가 다른 서류만 강등한다.
     * 파생 규칙: hasSlip→SLIP, hasTax(only)→TAX, 둘 다 없고 groupFinancialNo 있음→GROUPED,
     *             둘 다 없고 그룹 없음→CREATED.
     * CLEAR 상태(priority 4)인 서류는 취소 불가 — conflict 예외(트랜잭션 롤백).
     */
    private List<Long> demoteDocumentStatus(List<DocumentLineFlag> flags) {
        Map<String, List<Long>> groupedByDerived = new HashMap<>();

        for (DocumentLineFlag flag : flags) {
            DocumentStatus currentStatus = DocumentStatus.fromName(flag.currentDocumentStatus());

            // CLEAR 상태는 취소 불가
            if (currentStatus == DocumentStatus.CLEAR) {
                throw FmsException.conflict(
                    MessageCode.FREIGHT_LINE_CANCEL_NOT_ALLOWED.name(),
                    MessageCode.FREIGHT_LINE_CANCEL_NOT_ALLOWED.message()
                );
            }

            String derived = deriveStatus(flag);
            if (derived == null) continue;

            DocumentStatus derivedStatus = DocumentStatus.fromName(derived);

            // null 가드 후, 재파생 상태가 현재와 다를 때만 강등 대상
            if (currentStatus != null && derivedStatus != null && !derived.equals(flag.currentDocumentStatus())) {
                groupedByDerived
                    .computeIfAbsent(derived, k -> new ArrayList<>())
                    .add(flag.financialDocumentId());
            }
        }

        List<Long> affectedIds = new ArrayList<>();
        groupedByDerived.forEach((status, ids) -> {
            financialDocumentPort.bulkUpdateDocumentStatus(ids, status);
            affectedIds.addAll(ids);
        });

        return Collections.unmodifiableList(affectedIds);
    }

    /**
     * 클리어 후 flag 기반 재파생 상태 결정.
     * hasSlip→SLIP, hasTax(only)→TAX, 없고 groupFinancialNo 있음→GROUPED, 없음→CREATED.
     */
    private String deriveStatus(DocumentLineFlag flag) {
        if (flag.hasSlip()) return DocumentStatus.SLIP.name();
        if (flag.hasTax()) return DocumentStatus.TAX.name();
        if (flag.groupFinancialNo() != null && !flag.groupFinancialNo().isBlank()) {
            return DocumentStatus.GROUPED.name();
        }
        return DocumentStatus.CREATED.name();
    }

    /**
     * 반환용 statusByDocumentId 맵 구성.
     * affectedIds에 포함된 서류는 재파생 상태, 나머지는 기존 상태(buildStatusMap 패턴).
     */
    private Map<Long, String> buildStatusMap(List<DocumentLineFlag> flags, List<Long> affectedIds) {
        Set<Long> affectedSet = Set.copyOf(affectedIds);
        Map<Long, String> result = new HashMap<>();

        for (DocumentLineFlag flag : flags) {
            if (affectedSet.contains(flag.financialDocumentId())) {
                result.put(flag.financialDocumentId(), deriveStatus(flag));
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
        return ids.stream().distinct().collect(Collectors.toList());
    }
}
