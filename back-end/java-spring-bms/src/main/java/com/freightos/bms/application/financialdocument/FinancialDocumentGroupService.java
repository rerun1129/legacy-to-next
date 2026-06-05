package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.command.ApplyGroupingCommand;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentGroupUseCase;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentPort;
import com.freightos.bms.application.financialdocument.port.out.GroupDocumentSnapshot;
import com.freightos.bms.application.financialdocument.port.out.GroupNumberGenerator;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.bms.domain.financialdocument.GroupNo;
import com.freightos.bms.domain.financialdocument.enums.DocumentStatus;
import com.freightos.bms.domain.financialdocument.enums.GroupCategory;
import com.freightos.common.exception.FmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 금융 서류 그룹 부여/해제 유스케이스 구현체.
 * FinancialDocumentService에 넣지 않고 별도 서비스로 분리(SRP).
 * 흐름: 스냅샷 로드 → 검증 → 그룹 번호 결정 → 부여 → 상태 승급 → 해제 → 상태 강등.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FinancialDocumentGroupService implements FinancialDocumentGroupUseCase {

    private final FinancialDocumentPort financialDocumentPort;
    private final GroupNumberGenerator groupNumberGenerator;

    @Override
    public GroupResult applyGrouping(ApplyGroupingCommand cmd) {
        List<Long> grouped = distinctOrEmpty(cmd.groupedDocumentIds());
        List<Long> scope = cmd.scopeDocumentIds() != null ? distinct(cmd.scopeDocumentIds()) : grouped;

        // union(scope ∪ grouped)으로 스냅샷 일괄 로드
        Set<Long> unionSet = new HashSet<>(scope);
        unionSet.addAll(grouped);
        List<Long> union = new ArrayList<>(unionSet);

        Map<Long, GroupDocumentSnapshot> byId = loadSnapshotMap(union);

        // scope에 없는 id가 요청에 있으면 conflict
        validateAllLoaded(union, byId);

        String resolvedGroupNo = null;

        if (!grouped.isEmpty()) {
            List<GroupDocumentSnapshot> groupedSnaps = grouped.stream()
                .map(byId::get)
                .toList();

            validateSameCustomer(groupedSnaps);
            validateSameCategory(groupedSnaps);

            final String assignGroupNo = resolveOrCreateGroupNo(groupedSnaps);
            resolvedGroupNo = assignGroupNo;

            // 아직 해당 그룹번호가 없는 서류에만 부여
            List<Long> assignIds = grouped.stream()
                .filter(id -> !assignGroupNo.equals(byId.get(id).groupFinancialNo()))
                .toList();

            if (!assignIds.isEmpty()) {
                financialDocumentPort.bulkAssignGroupNo(assignIds, assignGroupNo);
                promoteToGrouped(assignIds, byId);
            }
        }

        // scope 중 grouped에 없고 현재 그룹번호가 있는 서류는 해제
        List<Long> releaseIds = scope.stream()
            .filter(id -> !grouped.contains(id) && byId.get(id).groupFinancialNo() != null)
            .toList();

        if (!releaseIds.isEmpty()) {
            financialDocumentPort.bulkClearGroupNo(releaseIds);
            demoteFromGrouped(releaseIds, byId);
        }

        return new GroupResult(grouped.isEmpty() ? null : resolvedGroupNo, grouped, releaseIds);
    }

    // ── 검증 ──────────────────────────────────────────────────────────────────

    private void validateAllLoaded(List<Long> ids, Map<Long, GroupDocumentSnapshot> byId) {
        List<Long> missing = ids.stream().filter(id -> !byId.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_NOT_FOUND.name(),
                "존재하지 않는 서류 ID: " + missing
            );
        }
    }

    private void validateSameCustomer(List<GroupDocumentSnapshot> snaps) {
        long distinct = snaps.stream().map(GroupDocumentSnapshot::customerCode).distinct().count();
        if (distinct > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.name(),
                MessageCode.FINANCIAL_DOCUMENT_MIXED_CUSTOMER.message()
            );
        }
    }

    private void validateSameCategory(List<GroupDocumentSnapshot> snaps) {
        long distinct = snaps.stream()
            .map(s -> GroupCategory.fromDocumentTypeName(s.documentType()))
            .distinct()
            .count();
        if (distinct > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_GROUP_MIXED_CATEGORY.name(),
                MessageCode.FINANCIAL_DOCUMENT_GROUP_MIXED_CATEGORY.message()
            );
        }
    }

    // ── 그룹 번호 결정 ─────────────────────────────────────────────────────────

    /**
     * 그룹 스냅샷들의 기존 그룹번호를 분석해 최종 부여할 그룹 번호를 반환한다.
     * 기존 그룹 없음: 신규 채번. 기존 1개: 합류. 기존 2개 이상: conflict.
     */
    private String resolveOrCreateGroupNo(List<GroupDocumentSnapshot> groupedSnaps) {
        List<String> existing = groupedSnaps.stream()
            .map(GroupDocumentSnapshot::groupFinancialNo)
            .filter(gno -> gno != null && !gno.isBlank())
            .distinct()
            .toList();

        if (existing.size() > 1) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_GROUP_MULTIPLE_EXISTING.name(),
                MessageCode.FINANCIAL_DOCUMENT_GROUP_MULTIPLE_EXISTING.message()
            );
        }
        if (existing.size() == 1) {
            // 기존 그룹에 합류
            return existing.get(0);
        }

        // 신규 채번
        GroupDocumentSnapshot representative = groupedSnaps.get(0);
        GroupCategory category = GroupCategory.fromDocumentTypeName(representative.documentType());
        String yymm = extractYymm(representative.documentDt());
        int seq = groupNumberGenerator.nextSeq(category, yymm);
        return GroupNo.of(category, yymm, seq).value();
    }

    /**
     * documentDt(yyyyMMdd)에서 yymm(2자리연도+2자리월)을 추출한다.
     * null 또는 8자리 미만이면 시스템 시각이 아닌 conflict 예외(§6.12 documentDt 필수).
     */
    private String extractYymm(String documentDt) {
        if (documentDt == null || documentDt.length() < 6) {
            throw FmsException.conflict(
                MessageCode.FINANCIAL_DOCUMENT_NOT_FOUND.name(),
                "그룹 채번 기준 날짜(document_dt)가 유효하지 않습니다: " + documentDt
            );
        }
        return documentDt.substring(2, 6);
    }

    // ── 상태 승급/강등 ─────────────────────────────────────────────────────────

    /** GROUPED 우선순위 미만인 서류만 GROUPED로 승급한다. */
    private void promoteToGrouped(List<Long> assignIds, Map<Long, GroupDocumentSnapshot> byId) {
        int groupedPriority = DocumentStatus.GROUPED.priority();
        List<Long> promoteIds = assignIds.stream()
            .filter(id -> {
                DocumentStatus status = DocumentStatus.fromName(byId.get(id).documentStatus());
                return status != null && status.priority() < groupedPriority;
            })
            .toList();
        if (!promoteIds.isEmpty()) {
            financialDocumentPort.bulkUpdateDocumentStatus(promoteIds, DocumentStatus.GROUPED.name());
        }
    }

    /** GROUPED 상태인 서류만 CREATED로 강등한다. TAX 이상은 보존. */
    private void demoteFromGrouped(List<Long> releaseIds, Map<Long, GroupDocumentSnapshot> byId) {
        List<Long> demoteIds = releaseIds.stream()
            .filter(id -> DocumentStatus.GROUPED.name().equals(byId.get(id).documentStatus()))
            .toList();
        if (!demoteIds.isEmpty()) {
            financialDocumentPort.bulkUpdateDocumentStatus(demoteIds, DocumentStatus.CREATED.name());
        }
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private Map<Long, GroupDocumentSnapshot> loadSnapshotMap(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        return financialDocumentPort.loadGroupSnapshots(ids).stream()
            .collect(Collectors.toMap(GroupDocumentSnapshot::financialDocumentId, Function.identity()));
    }

    private List<Long> distinct(List<Long> ids) {
        if (ids == null) return Collections.emptyList();
        return ids.stream().distinct().toList();
    }

    private List<Long> distinctOrEmpty(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return ids.stream().distinct().toList();
    }
}
