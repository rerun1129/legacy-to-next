package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * PMS 집계 쿼리 아웃바운드 어댑터.
 * freight_line 기반 / document 기반 단일 쿼리 레포지토리를 PmsPerformanceQueryPort로 노출.
 *
 * 단일 쿼리(page CTE + identity/cargo/name JOIN) 전환 이후 PmsCargoQueryPort 제거됨.
 * cargo + identity + name 정보는 각 레포지토리가 반환하는 PmsRawBlRow에 포함되어 있다.
 */
@Component
@RequiredArgsConstructor
public class PmsPerformancePersistenceAdapter implements PmsPerformanceQueryPort {

    private final PmsFreightLineAggregateQueryRepository freightLineRepo;
    private final PmsDocumentAggregateQueryRepository documentRepo;

    @Override
    public Page<PmsRawBlRow> searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable) {
        return freightLineRepo.search(command, pageable);
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        return documentRepo.search(command, pageable);
    }
}
