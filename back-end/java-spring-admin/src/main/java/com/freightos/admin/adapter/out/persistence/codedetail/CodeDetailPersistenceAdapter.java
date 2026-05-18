package com.freightos.admin.adapter.out.persistence.codedetail;

import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.port.out.CodeDetailPort;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CodeDetailPersistenceAdapter implements CodeDetailPort {

    private final CodeDetailRepository codeDetailRepository;
    private final CodeDetailDomainToJpaMapper codeDetailDomainToJpaMapper;
    private final CodeDetailJpaToDomainMapper codeDetailJpaToDomainMapper;

    @Override
    public PagedResult<CodeDetailSummary> searchSummaries(SearchCodeDetailCommand command) {
        return codeDetailRepository.searchSummaries(command);
    }

    @Override
    public Optional<CodeDetail> findCodeDetailById(Long id) {
        return codeDetailRepository.findById(id).map(codeDetailJpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(CodeDetail codeDetail) {
        CodeDetailJpaEntity entity = codeDetailDomainToJpaMapper.toNewJpa(codeDetail);
        codeDetailRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, CodeDetail patchData) {
        CodeDetailJpaEntity entity = codeDetailRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_DETAIL_NOT_FOUND", MessageCode.CODE_DETAIL_NOT_FOUND.getMessage()));
        codeDetailDomainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void deleteCodeDetailById(Long id) {
        if (!codeDetailRepository.existsById(id)) {
            throw ApplicationException.notFound("CODE_DETAIL_NOT_FOUND", MessageCode.CODE_DETAIL_NOT_FOUND.getMessage());
        }
        codeDetailRepository.deleteById(id);
    }

    @Override
    public long countByMasterId(Long masterId) {
        return codeDetailRepository.countByMasterId(masterId);
    }
}
