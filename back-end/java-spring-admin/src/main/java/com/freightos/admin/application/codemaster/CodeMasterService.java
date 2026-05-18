package com.freightos.admin.application.codemaster;

import com.freightos.admin.application.codedetail.port.out.CodeDetailPort;
import com.freightos.admin.application.codemaster.command.CreateCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.UpdateCodeMasterCommand;
import com.freightos.admin.application.codemaster.port.in.CodeMasterUseCase;
import com.freightos.admin.application.codemaster.port.out.CodeMasterPort;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeMasterService implements CodeMasterUseCase {

    private final CodeMasterPort codeMasterPort;
    private final CodeMasterFactory codeMasterFactory;
    private final CodeDetailPort codeDetailPort;

    @Override
    public PagedResult<CodeMasterSummary> searchCodeMasters(SearchCodeMasterCommand command) {
        return codeMasterPort.searchSummaries(command);
    }

    @Override
    public CodeMaster findCodeMasterById(Long id) {
        return codeMasterPort.findCodeMasterById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_MASTER_NOT_FOUND", MessageCode.CODE_MASTER_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createCodeMaster(CreateCodeMasterCommand command) {
        try {
            CodeMaster codeMaster = codeMasterFactory.from(command);
            return codeMasterPort.save(codeMaster);
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("CODE_MASTER_DUPLICATE_CODE", MessageCode.CODE_MASTER_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateCodeMaster(Long id, UpdateCodeMasterCommand command) {
        CodeMaster existing = codeMasterPort.findCodeMasterById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_MASTER_NOT_FOUND", MessageCode.CODE_MASTER_NOT_FOUND.getMessage()));
        existing.applyUpdate(command.masterName(), command.description(), command.sortOrder(), command.active());
        codeMasterPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteCodeMasterById(Long id) {
        // 하위 코드 항목 존재 여부 검증 — 존재하면 삭제 불가
        if (codeDetailPort.countByMasterId(id) > 0) {
            throw ApplicationException.conflict("CODE_MASTER_HAS_DETAIL_CANNOT_DELETE", MessageCode.CODE_MASTER_HAS_DETAIL_CANNOT_DELETE.getMessage());
        }
        codeMasterPort.deleteCodeMasterById(id);
    }

    @Override
    @Transactional
    public void deleteCodeMasters(List<Long> ids) {
        for (Long id : ids) {
            deleteCodeMasterById(id);
        }
    }
}
