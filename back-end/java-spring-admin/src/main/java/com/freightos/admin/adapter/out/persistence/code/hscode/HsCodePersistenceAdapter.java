package com.freightos.admin.adapter.out.persistence.code.hscode;

import com.freightos.admin.application.code.hscode.command.SearchHsCodeCommand;
import com.freightos.admin.application.code.hscode.port.out.HsCodePort;
import com.freightos.admin.application.code.hscode.projection.HsCodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.hscode.entity.HsCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HsCodePersistenceAdapter implements HsCodePort {

    private final HsCodeRepository hsCodeRepository;
    private final HsCodeDomainToJpaMapper domainToJpaMapper;
    private final HsCodeJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<HsCodeSummary> searchSummaries(SearchHsCodeCommand command) {
        return hsCodeRepository.searchSummaries(command);
    }

    @Override
    public Optional<HsCode> findById(Long id) {
        return hsCodeRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(HsCode hsCode) {
        HsCodeJpaEntity entity = domainToJpaMapper.toNewJpa(hsCode);
        hsCodeRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, HsCode patchData) {
        HsCodeJpaEntity entity = hsCodeRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("HS_CODE_NOT_FOUND", MessageCode.HS_CODE_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        HsCodeJpaEntity entity = hsCodeRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("HS_CODE_NOT_FOUND", MessageCode.HS_CODE_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return hsCodeRepository.autocomplete(query, limit);
    }
}
