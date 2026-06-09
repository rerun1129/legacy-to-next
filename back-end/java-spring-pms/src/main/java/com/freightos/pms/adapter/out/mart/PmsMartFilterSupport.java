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

        // 정형 서류조건(issued/grouped/documentStatus/documentTypes)은 pms_bl_mart pageCriteria로 sub-second 처리.
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
     * 정형 서류조건(issued/grouped/documentStatus/documentTypes) 존재 여부.
     * 이 조건들은 pms_bl_mart의 lines[]/docs[] 임베드 배열에 이미 적재돼 있어
     * pageCriteria($elemMatch) 경로로 sub-second 처리 가능하다.
     * PmsMartQueryAdapter의 라우팅·count/page 분기에서도 참조한다(static).
     */
    public static boolean hasDocLineFilter(SearchPmsPerformanceCommand c) {
        return StringUtils.hasText(c.issued())
            || StringUtils.hasText(c.grouped())
            || StringUtils.hasText(c.documentStatus())
            || (c.documentTypes() != null && !c.documentTypes().isEmpty());
    }

    // ── 기존 로직(line-accel OFF 기본 경로) ─────────────────────────────────────

    /**
     * line-accel 이전 판단 로직. line-accel OFF 시 그대로 사용되며,
     * line-accel ON 시에도 "line/doc 필터 존재 여부" 판별에 재사용된다.
     * 아래 line/document 레벨 필터가 하나라도 존재하면 false.
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

        // BMS 운임행 필터 (freight_line 레벨)
        if (StringUtils.hasText(c.financialDocType())) return false;
        if (StringUtils.hasText(c.taxType())) return false;
        if (StringUtils.hasText(c.issued())) return false;

        // BMS 서류 필터 (financial_document 레벨)
        if (StringUtils.hasText(c.documentStatus())) return false;
        if (StringUtils.hasText(c.documentNoLike())) return false;
        if (StringUtils.hasText(c.groupFinancialNo())) return false;
        if (StringUtils.hasText(c.grouped())) return false;

        return true;
    }

    // ── line-accel ON: basis별 sidecar 지원 가능 여부 ────────────────────────────

    /**
     * DOCUMENT_CREATED basis에서 sidecar(pms_docdt_entry)로 해소 가능한지 판단.
     * 날짜(실적일자 또는 서류일자) 없으면 dim-only → OLTP 폴백.
     * sidecar 미지원 필터(financialDocType, taxType, issued, documentNoLike, groupFinancialNo) 존재 시 OLTP 폴백.
     */
    private boolean isDocumentBasisSupported(SearchPmsPerformanceCommand c) {
        boolean hasDate = StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo())
                || StringUtils.hasText(c.documentDtFrom()) || StringUtils.hasText(c.documentDtTo());
        if (!hasDate) {
            return false;
        }

        // fd 레벨에 sidecar가 다루지 못하는 필터가 있으면 OLTP 폴백
        if (StringUtils.hasText(c.financialDocType())) return false;
        if (StringUtils.hasText(c.taxType())) return false;
        if (StringUtils.hasText(c.issued())) return false;
        if (StringUtils.hasText(c.documentNoLike())) return false;
        if (StringUtils.hasText(c.groupFinancialNo())) return false;

        // documentTypes, documentStatus, grouped, teamCode, operator, B/L 필터 = sidecar 지원
        return true;
    }

    /**
     * 정형 서류조건이 pms_bl_mart pageCriteria 경로로 처리 가능한지 판단.
     * pageCriteria가 다루지 못하는 비정형(taxType/documentNoLike/groupFinancialNo) 또는
     * PERFORMANCE dateKind(line-level 실적일자 — pageCriteria addDateRange가 ETD/ETA만 처리)면 OLTP 폴백.
     * (참고: 현재 FE는 실적일자를 performanceDtFrom/To로 보내고 dateKind=null이므로 PERFORMANCE 가드는 방어적.)
     */
    private boolean isDocLineFilterSupported(SearchPmsPerformanceCommand c) {
        if (StringUtils.hasText(c.taxType())) return false;
        if (StringUtils.hasText(c.documentNoLike())) return false;
        if (StringUtils.hasText(c.groupFinancialNo())) return false;
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
     * sidecar 미지원 필터(taxType, issued, operator, doc-only 필드) 존재 시 OLTP 폴백.
     */
    private boolean isFreightBasisSupported(SearchPmsPerformanceCommand c) {
        boolean hasDate = StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo());
        if (!hasDate) {
            return false;
        }

        // freight sidecar 미지원 필터
        if (StringUtils.hasText(c.taxType())) return false;
        if (StringUtils.hasText(c.issued())) return false;
        if (StringUtils.hasText(c.operator())) return false;

        // doc-only 필드: freight sidecar에는 없음
        if (StringUtils.hasText(c.documentStatus())) return false;
        if (StringUtils.hasText(c.documentDtFrom())) return false;
        if (StringUtils.hasText(c.documentDtTo())) return false;
        if (StringUtils.hasText(c.documentNoLike())) return false;
        if (StringUtils.hasText(c.groupFinancialNo())) return false;
        if (StringUtils.hasText(c.grouped())) return false;

        // documentTypes, financialDocType, B/L 필터 = sidecar 지원
        return true;
    }
}
