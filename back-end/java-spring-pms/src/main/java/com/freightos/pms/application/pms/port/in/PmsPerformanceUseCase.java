package com.freightos.pms.application.pms.port.in;

import com.freightos.pms.application.pms.PmsPerformanceSearchResult;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.data.domain.Pageable;

/**
 * PMS 실적 조회 인바운드 포트.
 */
public interface PmsPerformanceUseCase {

    PmsPerformanceSearchResult search(SearchPmsPerformanceCommand command, Pageable pageable);
}
