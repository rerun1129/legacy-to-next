package com.freightos.admin.adapter.out.persistence.code;

import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.port.out.CodePort;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.entity.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CodePersistenceAdapter implements CodePort {

    private final CodeRepository codeRepository;
    private final CodeDomainToJpaMapper codeDomainToJpaMapper;
    private final CodeJpaToDomainMapper codeJpaToDomainMapper;

    @Override
    public PagedResult<CodeSummary> searchSummaries(SearchCodeCommand command) {
        return codeRepository.searchSummaries(command);
    }

    @Override
    public Optional<Code> findById(Long id) {
        return codeRepository.findById(id).map(codeJpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Code code) {
        CodeJpaEntity entity = codeDomainToJpaMapper.toNewJpa(code);
        codeRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Code patchData) {
        CodeJpaEntity entity = codeRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_NOT_FOUND", MessageCode.CODE_NOT_FOUND.getMessage()));
        codeDomainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void deleteById(Long id) {
        if (!codeRepository.existsById(id)) {
            throw ApplicationException.notFound("CODE_NOT_FOUND", MessageCode.CODE_NOT_FOUND.getMessage());
        }
        codeRepository.deleteById(id);
    }
}
