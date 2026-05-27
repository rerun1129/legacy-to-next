package com.freightos.admin.application.code.hscode;

import com.freightos.admin.application.code.hscode.command.CreateHsCodeCommand;
import com.freightos.admin.application.code.hscode.command.SearchHsCodeCommand;
import com.freightos.admin.application.code.hscode.command.UpdateHsCodeCommand;
import com.freightos.admin.application.code.hscode.port.in.HsCodeUseCase;
import com.freightos.admin.application.code.hscode.port.out.HsCodePort;
import com.freightos.admin.application.code.hscode.projection.HsCodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.hscode.entity.HsCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HsCodeService implements HsCodeUseCase {

    private final HsCodePort hsCodePort;
    private final HsCodeFactory hsCodeFactory;

    @Override
    public PagedResult<HsCodeSummary> searchHsCodes(SearchHsCodeCommand command) {
        return hsCodePort.searchSummaries(command);
    }

    @Override
    public HsCode getHsCodeById(Long id) {
        return hsCodePort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("HS_CODE_NOT_FOUND", MessageCode.HS_CODE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createHsCode(CreateHsCodeCommand command) {
        try {
            return hsCodePort.save(hsCodeFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("HS_CODE_DUPLICATE_CODE", MessageCode.HS_CODE_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateHsCode(Long id, UpdateHsCodeCommand command) {
        HsCode hsCode = getHsCodeById(id);
        if (hsCode.isDeleted()) {
            throw ApplicationException.conflict("HS_CODE_ALREADY_DELETED", MessageCode.HS_CODE_ALREADY_DELETED.getMessage());
        }
        hsCode.applyUpdate(command.name(), command.nameEn(), command.countryCode(), command.active());
        hsCodePort.update(id, hsCode);
    }

    @Override
    @Transactional
    public void deleteHsCode(Long id) {
        HsCode hsCode = getHsCodeById(id);
        if (hsCode.isDeleted()) {
            throw ApplicationException.conflict("HS_CODE_ALREADY_DELETED", MessageCode.HS_CODE_ALREADY_DELETED.getMessage());
        }
        hsCodePort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteHsCodes(List<Long> ids) {
        for (Long id : ids) {
            deleteHsCode(id);
        }
    }
}
