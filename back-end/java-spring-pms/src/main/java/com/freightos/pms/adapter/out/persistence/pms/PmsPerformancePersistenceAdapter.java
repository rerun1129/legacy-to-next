package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.PmsRawBlSearchResult;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * PMS 집계 쿼리 아웃바운드 어댑터.
 * freight_line 기반 / document 기반 단일 쿼리 레포지토리를 PmsPerformanceQueryPort로 노출.
 *
 * 단일 쿼리(page CTE + identity/cargo/name JOIN) 전환 이후 PmsCargoQueryPort 제거됨.
 * cargo + identity + name 정보는 각 레포지토리가 반환하는 PmsRawBlRow에 포함되어 있다.
 *
 * OLTP count는 항상 정확하므로 approximateTotal=false로 고정한다.
 */
@Component
@RequiredArgsConstructor
public class PmsPerformancePersistenceAdapter implements PmsPerformanceQueryPort {

    private final PmsFreightLineAggregateQueryRepository freightLineRepo;
    private final PmsDocumentAggregateQueryRepository documentRepo;

    @Override
    public PmsRawBlSearchResult searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable) {
        return PmsRawBlSearchResult.exact(freightLineRepo.search(command, pageable));
    }

    @Override
    public PmsRawBlSearchResult searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        return PmsRawBlSearchResult.exact(documentRepo.search(command, pageable));
    }
}
