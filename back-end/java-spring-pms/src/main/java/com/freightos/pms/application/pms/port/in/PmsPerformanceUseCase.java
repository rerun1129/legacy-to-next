package com.freightos.pms.application.pms.port.in;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.projection.PmsPerformanceRowView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PMS 실적 조회 인바운드 포트.
 */
public interface PmsPerformanceUseCase {

    Page<PmsPerformanceRowView> search(SearchPmsPerformanceCommand command, Pageable pageable);
}
