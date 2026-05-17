package com.freightos.admin.application.code;

import com.freightos.admin.application.code.command.CreateCodeCommand;
import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.command.UpdateCodeCommand;
import com.freightos.admin.application.code.port.in.CodeUseCase;
import com.freightos.admin.application.code.port.out.CodePort;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.entity.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService implements CodeUseCase {

    private final CodePort codePort;
    private final CodeFactory codeFactory;

    @Override
    public PagedResult<CodeSummary> searchCodes(SearchCodeCommand command) {
        return codePort.searchSummaries(command);
    }

    @Override
    public Code findCodeById(Long id) {
        return codePort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_NOT_FOUND", MessageCode.CODE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createCode(CreateCodeCommand command) {
        Code code = codeFactory.from(command);
        return codePort.save(code);
    }

    @Override
    @Transactional
    public void updateCode(Long id, UpdateCodeCommand command) {
        Code existing = codePort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_NOT_FOUND", MessageCode.CODE_NOT_FOUND.getMessage()));
        existing.applyUpdate(command.codeLabel(), command.sortOrder(), command.active(), command.remark());
        codePort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteCodeById(Long id) {
        codePort.deleteById(id);
    }
}
