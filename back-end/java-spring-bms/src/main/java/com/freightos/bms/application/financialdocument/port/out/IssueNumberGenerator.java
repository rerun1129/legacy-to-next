package com.freightos.bms.application.financialdocument.port.out;

import com.freightos.bms.domain.financialdocument.enums.IssueType;

/**
 * 발급 번호 채번 아웃바운드 포트.
 * issue_no_seq 테이블의 원자적 UPSERT RETURNING으로 다음 시퀀스를 반환한다.
 */
public interface IssueNumberGenerator {

    /**
     * 해당 발급 종류·월의 다음 시퀀스를 원자적으로 채번한다.
     *
     * @param issueType 발급 종류 (TAX / SLIP)
     * @param yymm      발급 연월 yyyyMMdd substring(2,6)
     * @return 채번된 시퀀스 값 (1부터 시작, 월별 리셋)
     */
    int nextSeq(IssueType issueType, String yymm);
}
