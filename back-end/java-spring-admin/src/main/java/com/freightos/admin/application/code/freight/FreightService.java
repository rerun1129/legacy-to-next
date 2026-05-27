package com.freightos.admin.application.code.freight;

import com.freightos.admin.application.code.freight.command.CreateFreightCommand;
import com.freightos.admin.application.code.freight.command.SearchFreightCommand;
import com.freightos.admin.application.code.freight.command.UpdateFreightCommand;
import com.freightos.admin.application.code.freight.port.in.FreightUseCase;
import com.freightos.admin.application.code.freight.port.out.FreightPort;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.freight.entity.Freight;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreightService implements FreightUseCase {

    private final FreightPort freightPort;
    private final FreightFactory freightFactory;

    @Override
    public PagedResult<FreightSummary> searchFreights(SearchFreightCommand command) {
        return freightPort.searchSummaries(command);
    }

    @Override
    public Freight getFreightById(Long id) {
        return freightPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FREIGHT_NOT_FOUND", MessageCode.FREIGHT_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createFreight(CreateFreightCommand command) {
        try {
            return freightPort.save(freightFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("FREIGHT_DUPLICATE_CODE", MessageCode.FREIGHT_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateFreight(Long id, UpdateFreightCommand command) {
        Freight freight = getFreightById(id);
        if (freight.isDeleted()) {
            throw ApplicationException.conflict("FREIGHT_ALREADY_DELETED", MessageCode.FREIGHT_ALREADY_DELETED.getMessage());
        }
        freight.applyUpdate(command.name(), command.nameEn(), command.description(), command.freightUnit(), command.freightGroup(), command.active());
        freightPort.update(id, freight);
    }

    @Override
    @Transactional
    public void deleteFreight(Long id) {
        Freight freight = getFreightById(id);
        if (freight.isDeleted()) {
            throw ApplicationException.conflict("FREIGHT_ALREADY_DELETED", MessageCode.FREIGHT_ALREADY_DELETED.getMessage());
        }
        freightPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteFreights(List<Long> ids) {
        for (Long id : ids) {
            deleteFreight(id);
        }
    }
}
