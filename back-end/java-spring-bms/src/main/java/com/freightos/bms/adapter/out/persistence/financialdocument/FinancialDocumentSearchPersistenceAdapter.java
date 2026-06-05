package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.FreightLineDetailView;
import com.freightos.bms.application.financialdocument.FinancialDocumentSearchView;
import com.freightos.bms.application.financialdocument.SearchFinancialDocumentCriteria;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FinancialDocumentSearchPort 아웃바운드 어댑터 구현.
 * 검색·디테일 조회만 담당(쓰기 없음).
 */
@Component
@RequiredArgsConstructor
public class FinancialDocumentSearchPersistenceAdapter implements FinancialDocumentSearchPort {

    private final FinancialDocumentSearchQueryRepository searchQueryRepository;

    @Override
    public Page<FinancialDocumentSearchView> search(
            SearchFinancialDocumentCriteria criteria, Pageable pageable) {
        return searchQueryRepository.search(criteria, pageable);
    }

    @Override
    public List<FreightLineDetailView> findLinesByDocument(Long documentId) {
        return searchQueryRepository.findLinesByDocument(documentId);
    }
}
