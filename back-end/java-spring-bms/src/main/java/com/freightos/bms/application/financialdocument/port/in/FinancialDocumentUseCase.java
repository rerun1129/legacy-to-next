package com.freightos.bms.application.financialdocument.port.in;

import com.freightos.bms.application.financialdocument.FinancialDocumentView;
import com.freightos.bms.application.financialdocument.IssuableLineView;
import com.freightos.bms.application.financialdocument.IssueResult;
import com.freightos.bms.application.financialdocument.command.IssueDocumentCommand;

import java.util.List;

/**
 * 금융 서류 인바운드 포트. 발행·삭제·조회 유스케이스를 정의한다.
 */
public interface FinancialDocumentUseCase {

    /**
     * 금융 서류 발행.
     * 선택된 운임 라인으로 서류를 생성하고 채번 후 라인에 연결한다.
     */
    IssueResult issueDocument(IssueDocumentCommand cmd);

    /**
     * 금융 서류 삭제.
     * 연결된 라인의 financial_document_id를 null로 해제한 뒤 서류를 삭제한다.
     * performance_dt는 원복하지 않는다(유지).
     */
    void deleteDocument(Long financialDocumentId);

    /** B/L에 속한 금융 서류 목록 조회. */
    List<FinancialDocumentView> findDocumentsByBl(String blType, String blId);

    /** B/L의 발행 가능 운임 라인 목록 조회(미발행+이미발행 혼재, 발행 여부 표시). */
    List<IssuableLineView> findIssuableLines(String blType, String blId, String freightType);
}
