package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.port.out.IssueNumberGenerator;
import com.freightos.bms.domain.financialdocument.enums.IssueType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * IssueNumberGenerator 아웃바운드 포트 구현체.
 * IssueNoSeqJdbcRepository를 통해 issue_no_seq 테이블에서 원자적으로 채번한다.
 * DocumentNumberGeneratorAdapter / GroupNumberGeneratorAdapter 패턴 복제.
 */
@Component
@RequiredArgsConstructor
public class IssueNumberGeneratorAdapter implements IssueNumberGenerator {

    private final IssueNoSeqJdbcRepository issueNoSeqJdbcRepository;

    @Override
    public int nextSeq(IssueType issueType, String yymm) {
        // issueType.name() = "TAX" / "SLIP" — UPSERT 키에 맞는 DB 저장값
        return issueNoSeqJdbcRepository.upsertNextSeq(issueType.name(), yymm);
    }
}
