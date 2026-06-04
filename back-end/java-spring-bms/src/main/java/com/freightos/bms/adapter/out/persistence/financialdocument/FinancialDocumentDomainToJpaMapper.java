package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.domain.financialdocument.FinancialDocument;
import org.springframework.stereotype.Component;

/**
 * FinancialDocument(Domain) → FinancialDocumentJpaEntity(JPA) 변환 매퍼.
 * 도메인 → 인프라 단방향.
 */
@Component
public class FinancialDocumentDomainToJpaMapper {

    public FinancialDocumentJpaEntity toJpaEntity(FinancialDocument domain) {
        FinancialDocumentJpaEntity entity = new FinancialDocumentJpaEntity();
        entity.setDocumentNo(domain.getDocumentNo());
        entity.setDocumentType(domain.getDocumentType().name());
        entity.setDocumentDt(domain.getDocumentDt());
        entity.setDocumentStatus(domain.getStatus().name());
        entity.setCustomerCode(domain.getCustomerCode());
        entity.setSettleTotalAmount(domain.getSettleTotalAmount());
        entity.setLocalTotalAmount(domain.getLocalTotalAmount());
        entity.setSettleTotalVat(domain.getSettleTotalVat());
        entity.setLocalTotalVat(domain.getLocalTotalVat());
        entity.setUsdTotalAmount(domain.getUsdTotalAmount());
        entity.setPerformanceDt(domain.getPerformanceDt());
        entity.setTeamCode(domain.getTeamCode());
        entity.setOperator(domain.getOperator());
        return entity;
    }
}
