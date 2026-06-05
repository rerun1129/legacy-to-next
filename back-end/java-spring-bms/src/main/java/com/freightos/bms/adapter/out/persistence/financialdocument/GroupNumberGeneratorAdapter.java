package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.port.out.GroupNumberGenerator;
import com.freightos.bms.domain.financialdocument.enums.GroupCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GroupNumberGenerator 아웃바운드 어댑터 구현.
 * GroupNoSeqJdbcRepository에 위임한다.
 */
@Component
@RequiredArgsConstructor
public class GroupNumberGeneratorAdapter implements GroupNumberGenerator {

    private final GroupNoSeqJdbcRepository seqRepository;

    @Override
    public int nextSeq(GroupCategory category, String yymm) {
        return seqRepository.upsertNextSeq(category.name(), yymm);
    }
}
