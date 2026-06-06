package com.freightos.bms.application.financialdocument.port.in;

import com.freightos.bms.application.financialdocument.FreightLineIssueRowView;
import com.freightos.bms.application.financialdocument.IssueFreightLineResult;
import com.freightos.bms.application.financialdocument.SearchFreightLineCriteria;
import com.freightos.bms.application.financialdocument.command.IssueFreightLineCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 운임 행 발급 인바운드 포트.
 * 세금계산서(TAX) / 전표(SLIP) 발급 공통 유스케이스.
 */
public interface FreightLineIssueUseCase {

    /**
     * 운임 행 전역 조회. 서류 발행된 행만 노출, 발급 번호/일/상태 포함.
     */
    Page<FreightLineIssueRowView> searchFreightLines(SearchFreightLineCriteria criteria, Pageable pageable);

    /**
     * 발급 실행 (세금계산서 또는 전표). cmd.issueType으로 분기.
     * 채번 → 라인 기록 → 서류 상태 승급.
     */
    IssueFreightLineResult issue(IssueFreightLineCommand cmd);
}
