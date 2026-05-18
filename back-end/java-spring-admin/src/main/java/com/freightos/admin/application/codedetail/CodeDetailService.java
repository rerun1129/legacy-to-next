package com.freightos.admin.application.codedetail;

import com.freightos.admin.application.codedetail.command.CreateCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.UpdateCodeDetailCommand;
import com.freightos.admin.application.codedetail.port.in.CodeDetailUseCase;
import com.freightos.admin.application.codedetail.port.out.CodeDetailPort;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.application.codemaster.port.out.CodeMasterPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        CodeDetail codeDetail = codeDetailFactory.from(command);
        return codeDetailPort.save(codeDetail);
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
}
