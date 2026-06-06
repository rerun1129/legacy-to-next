package com.freightos.bms.application.financialdocument.port.out;

import com.freightos.bms.application.financialdocument.SearchFreightLineCriteria;
import com.freightos.bms.application.financialdocument.FreightLineIssueRowView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 운임 행 발급 아웃바운드 포트.
 * bulkUpdateDocumentStatus는 기존 FinancialDocumentPort에 이미 존재하므로
 * 이 포트에는 발급 고유 메서드(search / loadIssueLinesByIds / bulk tax·slip / flags)만 정의한다.
 */
public interface FreightLineIssuePort {

    /**
     * 운임 행 전역 조회(발급 화면 그리드).
     * financial_document_id IS NOT NULL 고정(서류 발행된 행만). 페이징 지원.
     */
    Page<FreightLineIssueRowView> searchFreightLines(SearchFreightLineCriteria criteria, Pageable pageable);

    /**
     * lineIds에 해당하는 발급용 스냅샷을 로드한다.
     * taxNo/slipNo 포함 — 이미발급 검증에 사용.
     * Tuple projection으로 로드해 bulk update 후 1차캐시 staleness를 회피한다.
     */
    List<FreightLineIssueSnapshot> loadIssueLinesByIds(List<Long> lineIds);

    /**
     * 지정 라인들의 tax_no·tax_dt를 일괄 기록한다.
     * QueryDSL 벌크 UPDATE — 영속 컨텍스트 bypass, .execute() 즉시 반영.
     */
    void bulkUpdateLineTax(List<Long> lineIds, String taxNo, String taxDt);

    /**
     * 지정 라인들의 slip_no·slip_dt를 일괄 기록한다.
     * QueryDSL 벌크 UPDATE — 영속 컨텍스트 bypass, .execute() 즉시 반영.
     */
    void bulkUpdateLineSlip(List<Long> lineIds, String slipNo, String slipDt);

    /**
     * 지정 서류들의 라인별 tax_no/slip_no 존재 여부와 현재 document_status를 집계한다.
     * bulk update 후 DB 재조회로 서류별 상태 재파생에 사용.
     * Tuple projection — 1차캐시 staleness 회피.
     */
    List<DocumentLineFlag> loadDocumentTaxSlipFlags(List<Long> documentIds);
}
