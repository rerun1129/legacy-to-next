package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.out.PmsCargoQueryPort;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import com.freightos.pms.application.pms.projection.PmsCargoRow;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PMS 집계 쿼리 아웃바운드 어댑터. 두 포트(PmsPerformanceQueryPort, PmsCargoQueryPort)를 단일 빈으로 구현.
 * freight_line 기반 / document 기반 레포지토리를 오케스트레이션.
 */
@Component
@RequiredArgsConstructor
public class PmsPerformancePersistenceAdapter implements PmsPerformanceQueryPort, PmsCargoQueryPort {

    private final PmsFreightLineAggregateQueryRepository freightLineRepo;
    private final PmsDocumentAggregateQueryRepository documentRepo;
    private final PmsCargoLookupQueryRepository cargoRepo;

    @Override
    public Page<PmsRawBlRow> searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable) {
        return freightLineRepo.search(command, pageable);
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        return documentRepo.search(command, pageable);
    }

    @Override
    public List<PmsCargoRow> findCargoByHouseBlIds(List<Long> houseBlIds) {
        return cargoRepo.fetchByHouseBlIds(houseBlIds);
    }
}
