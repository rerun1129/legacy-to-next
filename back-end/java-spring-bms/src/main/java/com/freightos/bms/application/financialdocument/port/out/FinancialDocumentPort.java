package com.freightos.bms.application.financialdocument.port.out;

import com.freightos.bms.application.financialdocument.FinancialDocumentView;
import com.freightos.bms.application.financialdocument.IssuableLineView;
import com.freightos.bms.domain.financialdocument.FinancialDocument;

import java.util.List;
import java.util.Optional;

/**
 * 금융 서류 아웃바운드 포트.
 * Application 계층 정의 — 어댑터 구현체를 직접 참조하지 않음.
 */
public interface FinancialDocumentPort {

    /**
     * 선택 라인 목록 로드. 금액 5종·customer_code·financial_doc_type 등 스냅샷 포함.
     */
    List<FreightLineSnapshot> loadLinesByIds(List<Long> lineIds);

    /** 금융 서류 저장 후 생성된 PK 반환. */
    Long saveDocument(FinancialDocument document);

    /**
     * 운임 라인에 서류 ID를 연결하고 performance_dt를 전파한다(§6.15).
     */
    void linkLines(List<Long> lineIds, Long documentId, String performanceDt);

    /**
     * 서류에 연결된 모든 라인의 financial_document_id를 null로 해제한다.
     * performance_dt는 유지한다.
     */
    void unlinkLinesByDocument(Long documentId);

    /** 금융 서류 삭제. */
    void deleteDocument(Long documentId);

    /** blType+blId로 freight_header_id 조회. 헤더 없으면 empty. */
    Optional<Long> findHeaderId(String blType, String blId);

    /** B/L에 속한 금융 서류 목록 조회(customerName 미포함 — Service에서 resolve). */
    List<FinancialDocumentView> findDocumentsByBl(String blType, String blId);

    /** B/L의 운임 라인 목록 조회(발행 여부 포함 — documentNo는 JOIN으로 채워짐, customerName 미포함). */
    List<IssuableLineView> findIssuableLines(Long headerId, String freightType);
}
