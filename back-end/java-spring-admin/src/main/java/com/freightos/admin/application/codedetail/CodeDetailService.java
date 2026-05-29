package com.freightos.admin.application.codedetail;

import com.freightos.admin.application.codedetail.command.CreateCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.SaveCodeDetailChangesCommand;
import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.UpdateCodeDetailCommand;
import com.freightos.admin.application.codedetail.port.in.CodeDetailUseCase;
import com.freightos.admin.application.codedetail.port.out.CodeDetailPort;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.application.codemaster.port.out.CodeMasterPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeDetailService implements CodeDetailUseCase {

    private final CodeDetailPort codeDetailPort;
    private final CodeDetailFactory codeDetailFactory;
    private final CodeMasterPort codeMasterPort;

    @Override
    public PagedResult<CodeDetailSummary> searchCodeDetails(SearchCodeDetailCommand command) {
        return codeDetailPort.searchSummaries(command);
    }

    @Override
    public CodeDetail findCodeDetailById(Long id) {
        return codeDetailPort.findCodeDetailById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_DETAIL_NOT_FOUND", MessageCode.CODE_DETAIL_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createCodeDetail(CreateCodeDetailCommand command) {
        // masterId 유효성 검증 — 존재하지 않으면 생성 불가
        if (!codeMasterPort.existsById(command.masterId())) {
            throw ApplicationException.notFound("CODE_MASTER_NOT_FOUND", MessageCode.CODE_MASTER_NOT_FOUND.getMessage());
        }
        try {
            CodeDetail codeDetail = codeDetailFactory.from(command);
            return codeDetailPort.save(codeDetail);
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("CODE_DUPLICATE_DETAIL_VALUE", MessageCode.CODE_DUPLICATE_DETAIL_VALUE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateCodeDetail(Long id, UpdateCodeDetailCommand command) {
        CodeDetail existing = codeDetailPort.findCodeDetailById(id)
                .orElseThrow(() -> ApplicationException.notFound("CODE_DETAIL_NOT_FOUND", MessageCode.CODE_DETAIL_NOT_FOUND.getMessage()));
        existing.applyUpdate(command.codeLabel(), command.sortOrder(), command.active(), command.remark());
        codeDetailPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteCodeDetailById(Long id) {
        codeDetailPort.deleteCodeDetailById(id);
    }

    @Override
    @Transactional
    public void deleteCodeDetails(List<Long> ids) {
        for (Long id : ids) {
            deleteCodeDetailById(id);
        }
    }

    @Override
    @Transactional
    public SaveChangesResult saveCodeDetailChanges(SaveCodeDetailChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deleteCodeDetailById(id);
        }
        for (SaveCodeDetailChangesCommand.UpdateEntry entry : command.updates()) {
            updateCodeDetail(entry.id(), entry.command());
        }
        for (CreateCodeDetailCommand create : command.creates()) {
            createCodeDetail(create);
        }
        return new SaveChangesResult(
                command.creates().size(),
                command.updates().size(),
                command.deleteIds().size()
        );
    }
}
