package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.FreightLineIssueRowView;
import com.freightos.bms.application.financialdocument.SearchFreightLineCriteria;
import com.freightos.bms.application.financialdocument.port.out.DocumentLineFlag;
import com.freightos.bms.application.financialdocument.port.out.FreightLineIssuePort;
import com.freightos.bms.application.financialdocument.port.out.FreightLineIssueSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FreightLineIssuePort 아웃바운드 어댑터 구현.
 * 발급 전용 메서드(search / loadIssueLinesByIds / bulkUpdateLineTax·Slip / loadDocumentTaxSlipFlags)만 위임.
 * bulkUpdateDocumentStatus는 기존 FinancialDocumentPort(FinancialDocumentPersistenceAdapter) 경유(S4).
 */
@Component
@RequiredArgsConstructor
public class FreightLineIssuePersistenceAdapter implements FreightLineIssuePort {

    private final FreightLineIssueQueryRepository issueQueryRepository;

    @Override
    public Page<FreightLineIssueRowView> searchFreightLines(
            SearchFreightLineCriteria criteria, Pageable pageable) {
        return issueQueryRepository.searchFreightLines(criteria, pageable);
    }

    @Override
    public List<FreightLineIssueSnapshot> loadIssueLinesByIds(List<Long> lineIds) {
        return issueQueryRepository.loadIssueLinesByIds(lineIds);
    }

    @Override
    public void bulkUpdateLineTax(List<Long> lineIds, String taxNo, String taxDt) {
        issueQueryRepository.bulkUpdateLineTax(lineIds, taxNo, taxDt);
    }

    @Override
    public void bulkUpdateLineSlip(List<Long> lineIds, String slipNo, String slipDt) {
        issueQueryRepository.bulkUpdateLineSlip(lineIds, slipNo, slipDt);
    }

    @Override
    public List<DocumentLineFlag> loadDocumentTaxSlipFlags(List<Long> documentIds) {
        return issueQueryRepository.loadDocumentTaxSlipFlags(documentIds);
    }

    @Override
    public void bulkClearLineTax(List<Long> lineIds) {
        issueQueryRepository.bulkClearLineTax(lineIds);
    }

    @Override
    public void bulkClearLineSlip(List<Long> lineIds) {
        issueQueryRepository.bulkClearLineSlip(lineIds);
    }
}
