package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.FinancialDocumentView;
import com.freightos.bms.application.financialdocument.IssuableLineView;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentPort;
import com.freightos.bms.application.financialdocument.port.out.FreightLineSnapshot;
import com.freightos.bms.domain.financialdocument.FinancialDocument;
import com.freightos.common.exception.FmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * FinancialDocumentPort 아웃바운드 어댑터 구현.
 * bms.financial_document + bms.freight_line 영속화 담당.
 */
@Component
@RequiredArgsConstructor
public class FinancialDocumentPersistenceAdapter implements FinancialDocumentPort {

    private final FinancialDocumentRepository documentRepository;
    private final FreightLineQueryRepository lineQueryRepository;
    private final FreightHeaderRefRepository headerRefRepository;
    private final FinancialDocumentDomainToJpaMapper domainToJpaMapper;

    @Override
    public List<FreightLineSnapshot> loadLinesByIds(List<Long> lineIds) {
        return lineQueryRepository.loadLinesByIds(lineIds);
    }

    @Override
    public Long saveDocument(FinancialDocument document) {
        FinancialDocumentJpaEntity entity = domainToJpaMapper.toJpaEntity(document);
        return documentRepository.save(entity).getFinancialDocumentId();
    }

    @Override
    public void linkLines(List<Long> lineIds, Long documentId, String performanceDt) {
        // 발행 서류 존재 확인
        if (!documentRepository.existsById(documentId)) {
            throw FmsException.conflict("FINANCIAL_DOCUMENT_NOT_FOUND", "서류 ID를 찾을 수 없습니다: " + documentId);
        }
        // QueryDSL 벌크 UPDATE로 라인 연결 + performance_dt 전파
        lineQueryRepository.bulkLinkLines(lineIds, documentId, performanceDt);
    }

    @Override
    public void unlinkLinesByDocument(Long documentId) {
        lineQueryRepository.bulkUnlinkLines(documentId);
    }

    @Override
    public void deleteDocument(Long documentId) {
        documentRepository.deleteById(documentId);
    }

    @Override
    public Optional<Long> findHeaderId(String blType, String blId) {
        return headerRefRepository
            .findByBlTypeAndBlId(blType, blId)
            .map(FreightHeaderRefJpaEntity::getFreightHeaderId);
    }

    @Override
    public List<FinancialDocumentView> findDocumentsByBl(String blType, String blId) {
        return lineQueryRepository.findDocumentsByBl(blType, blId);
    }

    @Override
    public List<IssuableLineView> findIssuableLines(Long headerId, String freightType) {
        return lineQueryRepository.findIssuableLines(headerId, freightType);
    }
}
