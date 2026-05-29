package com.freightos.admin.adapter.out.persistence.codemaster;

import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.port.out.CodeMasterPort;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CodeMasterPersistenceAdapter implements CodeMasterPort {

    private final CodeMasterRepository codeMasterRepository;
    private final CodeMasterDomainToJpaMapper codeMasterDomainToJpaMapper;
    private final CodeMasterJpaToDomainMapper codeMasterJpaToDomainMapper;

    @Override
    public PagedResult<CodeMasterSummary> searchSummaries(SearchCodeMasterCommand command) {
        return codeMasterRepository.searchSummaries(command);
    }

    @Override
    public Optional<CodeMaster> findCodeMasterById(Long id) {
        return codeMasterRepository.findById(id).map(codeMasterJpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(CodeMaster codeMaster) {
        CodeMasterJpaEntity entity = codeMasterDomainToJpaMapper.toNewJpa(codeMaster);
        codeMasterRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, CodeMaster patchData) {
        CodeMasterJpaEntity entity = codeMasterRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_MASTER_NOT_FOUND", MessageCode.CODE_MASTER_NOT_FOUND.getMessage()));
        codeMasterDomainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void deleteCodeMasterById(Long id) {
        if (!codeMasterRepository.existsById(id)) {
            throw ApplicationException.notFound("CODE_MASTER_NOT_FOUND", MessageCode.CODE_MASTER_NOT_FOUND.getMessage());
        }
        codeMasterRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return codeMasterRepository.existsById(id);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return codeMasterRepository.autocomplete(query, limit);
    }
}
