package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Mart 라우터가 MongoDB 어댑터를 사용할 수 있는지 판단하는 컴포넌트.
 *
 * line/document 레벨(합산 모수 변경) 필터가 하나라도 있으면 false를 반환하여
 * OLTP 폴백으로 위임한다. Mart 문서는 B/L 단위 사전집계이므로 이 필터들을
 * 적용한 부분집합 재집계가 불가능하기 때문이다.
 *
 * 판단 근거:
 * - OLTP freight_line 레벨 술어(l.*): financialDocType, taxType, issued, documentTypes,
 *   performanceDt 범위
 * - OLTP financial_document 레벨 술어(fd.*): performanceDt 범위, documentTypes,
 *   documentStatus, documentNoLike, documentDtFrom/To, groupFinancialNo, grouped
 * - dateKind=="PERFORMANCE" + 날짜 범위: performance_dt는 line 레벨 → Mart 미지원
 */
@Component
public class PmsMartFilterSupport {

    /**
     * Mart 어댑터를 사용할 수 있으면 true, OLTP 폴백이 필요하면 false.
     * 아래 line/document 레벨 필터가 하나라도 존재하면 false.
     */
    public boolean supportedByMart(SearchPmsPerformanceCommand c) {
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
}
