package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Mart 라우터가 MongoDB 어댑터를 사용할 수 있는지 판단하는 컴포넌트.
 *
 * line-accel OFF: 기존 legacySupported 로직 그대로 동작(무회귀).
 * line-accel ON:  날짜(실적일자/서류일자)가 1차 가속기. sidecar 날짜 인덱스로
 *                 좁힌 뒤 B/L 레벨 + 차원 필터를 residual 적용한다.
 *                 날짜 없는 dim-only 또는 sidecar가 지원하지 않는 필터는 OLTP 폴백.
 *
 * W1-A: FE가 전송하지 않는 필드(financialDocType/taxType/documentNoLike/groupFinancialNo/
 *        operator/hblNo/mblNo) 체크를 제거. 잔존: documentTypes/documentStatus.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartFilterSupport {

    private final PmsMartProperties props;
    private final PmsMartReadiness readiness;

    /**
     * Mart 어댑터를 사용할 수 있으면 true, OLTP 폴백이 필요하면 false.
     * line-accel OFF이면 legacySupported를 그대로 위임한다.
     */
    public boolean supportedByMart(SearchPmsPerformanceCommand c) {
        // Mart 빌드 전·빌드 중에는 전부 OLTP 폴백
        if (!readiness.isReady()) {
            return false;
        }

        if (!props.getLineAccel().isEnabled()) {
            return legacySupported(c);
        }

        // line-accel ON: basis별 날짜 보유 여부 + 미지원 필터 검사
        boolean hasBeyondBlLevelFilter = !legacySupported(c);
        if (!hasBeyondBlLevelFilter) {
            // B/L-only 필터(legacySupported=true 케이스) → 기존 fast path 그대로
            return true;
        }

        // 정형 서류조건(documentStatus/documentTypes)은 pms_bl_mart pageCriteria로 sub-second 처리.
        // basis 분기(sidecar)보다 먼저 평가 → ETD/ETA·무날짜 + 서류조건 결합도 Mart 진입.
        if (hasDocLineFilter(c)) {
            return isDocLineFilterSupported(c);
        }

        // line/doc 레벨 필터가 하나 이상 존재하는 케이스: sidecar로 해소 가능한지 판단
        boolean isDocumentBasis = c.effectiveBasis() == AggregationBasis.DOCUMENT_CREATED;
        if (isDocumentBasis) {
            return isDocumentBasisSupported(c);
        } else {
            return isFreightBasisSupported(c);
        }
    }

    /**
     * 정형 서류조건(documentStatus/documentTypes) 존재 여부.
     * 이 조건들은 pms_bl_mart의 docs[] 임베드 배열에 이미 적재돼 있어
     * pageCriteria($elemMatch) 경로로 sub-second 처리 가능하다.
     * PmsMartQueryAdapter의 라우팅·count/page 분기에서도 참조한다(static).
     */
    public static boolean hasDocLineFilter(SearchPmsPerformanceCommand c) {
        return StringUtils.hasText(c.documentStatus())
            || (c.documentTypes() != null && !c.documentTypes().isEmpty());
    }

    /**
     * TAX/SLIP basis + 서류타입 필터 동시 적용 시 sidecar covered count는
     * (발급 플래그, 서류타입)이 같은 라인임을 보장하지 못해 과대 집계된다.
     * 이 조합에서는 라인-그레인 pageCriteria($elemMatch)로 count·page를 일관 처리한다.
     *
     * W1-A: financialDocType 제거 — documentTypes만 체크.
     */
    public static boolean needsLineGrainCorrelation(SearchPmsPerformanceCommand c) {
        AggregationBasis basis = c.effectiveBasis();
        boolean taxOrSlip = basis == AggregationBasis.TAX_ISSUED || basis == AggregationBasis.SLIP_ISSUED;
        boolean hasDocTypeFilter = c.documentTypes() != null && !c.documentTypes().isEmpty();
        return taxOrSlip && hasDocTypeFilter;
    }

    // ── 기존 로직(line-accel OFF 기본 경로) ─────────────────────────────────────

    /**
     * line-accel 이전 판단 로직. line-accel OFF 시 그대로 사용되며,
     * line-accel ON 시에도 "line/doc 필터 존재 여부" 판별에 재사용된다.
     * 아래 line/document 레벨 필터가 하나라도 존재하면 false.
     *
     * W1-A: financialDocType/taxType/documentNoLike/groupFinancialNo 체크 제거.
     */
    private boolean legacySupported(SearchPmsPerformanceCommand c) {
        // dateKind=="PERFORMANCE" + 날짜 범위: performance_dt는 line 레벨 필터
        if ("PERFORMANCE".equals(c.dateKind())
                && (StringUtils.hasText(c.dateFrom()) || StringUtils.hasText(c.dateTo()))) {
            return false;
        }

        // performanceDt 범위: freight_line / financial_document 레벨
        if (StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo())) {
            return false;
        }

        // documentDt 범위: financial_document 레벨
        if (StringUtils.hasText(c.documentDtFrom()) || StringUtils.hasText(c.documentDtTo())) {
            return false;
        }

        // documentTypes: line / document 레벨 — 모수 변경
        if (c.documentTypes() != null && !c.documentTypes().isEmpty()) {
            return false;
        }

        // 정형 서류 필터 (document 레벨)
        if (StringUtils.hasText(c.documentStatus())) return false;

        return true;
    }

    // ── line-accel ON: basis별 sidecar 지원 가능 여부 ────────────────────────────

    /**
     * DOCUMENT_CREATED basis에서 sidecar(pms_docdt_entry)로 해소 가능한지 판단.
     * 날짜(실적일자 또는 서류일자) 없으면 dim-only → OLTP 폴백.
     *
     * W1-A: financialDocType/taxType/documentNoLike/groupFinancialNo 체크 제거.
     *       이 필드들은 FE가 전송하지 않으므로 항상 null → 체크 불필요.
     */
    private boolean isDocumentBasisSupported(SearchPmsPerformanceCommand c) {
        boolean hasDate = StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo())
                || StringUtils.hasText(c.documentDtFrom()) || StringUtils.hasText(c.documentDtTo());
        if (!hasDate) {
            return false;
        }
        // documentTypes, documentStatus, B/L 필터 = sidecar 지원
        return true;
    }

    /**
     * 정형 서류조건이 pms_bl_mart pageCriteria 경로로 처리 가능한지 판단.
     * PERFORMANCE dateKind(line-level 실적일자 — pageCriteria addDateRange가 ETD/ETA만 처리)면 OLTP 폴백.
     * (참고: 현재 FE는 실적일자를 performanceDtFrom/To로 보내고 dateKind=null이므로 PERFORMANCE 가드는 방어적.)
     *
     * W1-A: taxType/documentNoLike/groupFinancialNo 체크 제거.
     */
    private boolean isDocLineFilterSupported(SearchPmsPerformanceCommand c) {
        if ("PERFORMANCE".equals(c.dateKind())
                && (StringUtils.hasText(c.dateFrom()) || StringUtils.hasText(c.dateTo()))) {
            return false;
        }
        return true;
    }

    /**
     * freight basis(FREIGHT_INPUT/TAX_ISSUED/SLIP_ISSUED)에서 sidecar(pms_perfdt_entry)로
     * 해소 가능한지 판단.
     * 날짜(실적일자) 없으면 OLTP 폴백.
     *
     * W1-A: taxType/operator/documentNoLike/groupFinancialNo 체크 제거.
     */
    private boolean isFreightBasisSupported(SearchPmsPerformanceCommand c) {
        boolean hasDate = StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo());
        if (!hasDate) {
            return false;
        }

        // doc-only 필드: freight sidecar에는 없음
        if (StringUtils.hasText(c.documentStatus())) return false;
        if (StringUtils.hasText(c.documentDtFrom())) return false;
        if (StringUtils.hasText(c.documentDtTo())) return false;

        // documentTypes, B/L 필터 = sidecar 지원
        return true;
    }
}
