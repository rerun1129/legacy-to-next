package com.freightos.pms.application.pms.port.out;

import com.freightos.pms.application.pms.PmsRawBlSearchResult;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.data.domain.Pageable;

/**
 * PMS 집계 쿼리 아웃바운드 포트.
 * FREIGHT_INPUT/TAX_ISSUED/SLIP_ISSUED는 freightLine 기반, DOCUMENT_CREATED는 document 기반.
 */
public interface PmsPerformanceQueryPort {

    /** freight_line 소스 기반 B/L 페이지 집계. */
    PmsRawBlSearchResult searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable);

    /** financial_document 소스 기반 B/L 페이지 집계. */
    PmsRawBlSearchResult searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable);
}
