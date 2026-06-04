package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.port.out.DocumentNumberGenerator;
import com.freightos.bms.domain.financialdocument.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * DocumentNumberGenerator 아웃바운드 어댑터 구현.
 * DocumentNoSeqJdbcRepository에 위임한다.
 */
@Component
@RequiredArgsConstructor
public class DocumentNumberGeneratorAdapter implements DocumentNumberGenerator {

    private final DocumentNoSeqJdbcRepository seqRepository;

    @Override
    public int nextSeq(DocumentType type, String yymm) {
        return seqRepository.upsertNextSeq(type.name(), yymm);
    }
}
