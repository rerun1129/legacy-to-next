package com.freightos.bms.application.financialdocument.port.out;

import com.freightos.bms.application.financialdocument.FreightLineDetailView;
import com.freightos.bms.application.financialdocument.FinancialDocumentSearchView;
import com.freightos.bms.application.financialdocument.SearchFinancialDocumentCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 금융 서류 전역 검색·디테일 조회 아웃바운드 포트.
 * 반환 뷰의 이름 필드(customerName·teamName·operatorName·freightName)는 빈 값으로 반환되며,
 * Application 계층(FinancialDocumentQueryService)에서 CodeNameResolver로 일괄 resolve한다.
 */
public interface FinancialDocumentSearchPort {

    /**
     * 금융 서류 전역 검색. documentTypes IN 필수.
     */
    Page<FinancialDocumentSearchView> search(SearchFinancialDocumentCriteria criteria, Pageable pageable);

    /**
     * 특정 서류에 속한 운임 라인 전 컬럼 조회.
     */
    List<FreightLineDetailView> findLinesByDocument(Long documentId);
}
