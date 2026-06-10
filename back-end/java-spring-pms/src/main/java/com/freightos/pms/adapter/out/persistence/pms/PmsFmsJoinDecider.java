package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.stereotype.Component;

/**
 * FMS 테이블(house_bl/master_bl) LEFT JOIN 필요 여부 판단 컴포넌트.
 * <p>
 * 필요한 경우에만 JOIN하여 freight_line 집계 쿼리에서
 * 수백만 행 스캔 비용을 줄인다.
 *
 * W1-A: FE가 전송하지 않는 필드(hblNo/mblNo/portCode/salesManCode/salesClass/
 *        incoterms/teamCode/partyCode) 체크 제거. jobDiv/bound + 날짜만 잔존.
 */
@Component
public class PmsFmsJoinDecider {

    /**
     * freight_line 집계에서 FMS LEFT JOIN이 필요한지 판단.
     * (a) dateKind=ETD/ETA 이면서 dateFrom 또는 dateTo가 있을 때, 또는
     * (b) jobDiv 또는 bound 필터가 있을 때 true.
     */
    public boolean fmsJoinNeeded(SearchPmsPerformanceCommand c) {
        return etdEtaDateFilterPresent(c)
            || hasValue(c.jobDiv())
            || hasValue(c.bound());
    }

    /**
     * financial_document 집계에서 FMS LEFT JOIN이 필요한지 판단.
     * jobDiv/bound + 날짜 필터가 있을 때 true.
     */
    public boolean fmsJoinNeededForDocument(SearchPmsPerformanceCommand c) {
        return etdEtaDateFilterPresent(c)
            || hasValue(c.jobDiv())
            || hasValue(c.bound());
    }

    private boolean etdEtaDateFilterPresent(SearchPmsPerformanceCommand c) {
        String dateKind = c.dateKind();
        return ("ETD".equals(dateKind) || "ETA".equals(dateKind))
            && (hasValue(c.dateFrom()) || hasValue(c.dateTo()));
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
