package com.freightos.admin.application.code.port;

import com.freightos.admin.application.code.port.command.CreatePortCommand;
import com.freightos.admin.application.code.port.command.SavePortChangesCommand;
import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.command.UpdatePortCommand;
import com.freightos.admin.application.code.port.port.in.PortUseCase;
import com.freightos.admin.application.code.port.port.out.PortPort;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.port.entity.Port;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortService implements PortUseCase {

    private final PortPort portPort;
    private final PortFactory portFactory;

    @Override
    public PagedResult<PortSummary> searchPorts(SearchPortCommand command) {
        return portPort.searchSummaries(command);
    }

    @Override
    public Port getPortById(Long id) {
        return portPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PORT_NOT_FOUND", MessageCode.PORT_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createPort(CreatePortCommand command) {
        try {
            return portPort.save(portFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("PORT_DUPLICATE_CODE", MessageCode.PORT_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updatePort(Long id, UpdatePortCommand command) {
        Port port = getPortById(id);
        if (port.isDeleted()) {
            throw ApplicationException.conflict("PORT_ALREADY_DELETED", MessageCode.PORT_ALREADY_DELETED.getMessage());
        }
        port.applyUpdate(command.name(), command.nameEn(), command.countryCode(), command.portType(), command.active());
        portPort.update(id, port);
    }

    @Override
    @Transactional
    public void deletePort(Long id) {
        Port port = getPortById(id);
        if (port.isDeleted()) {
            throw ApplicationException.conflict("PORT_ALREADY_DELETED", MessageCode.PORT_ALREADY_DELETED.getMessage());
        }
        portPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deletePorts(List<Long> ids) {
        for (Long id : ids) {
            deletePort(id);
        }
    }

    @Override
    @Transactional
    public SaveChangesResult savePortChanges(SavePortChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deletePort(id);
        }
        for (SavePortChangesCommand.UpdateEntry entry : command.updates()) {
            updatePort(entry.id(), entry.command());
        }
        for (CreatePortCommand create : command.creates()) {
            createPort(create);
        }
        return new SaveChangesResult(
                command.creates().size(),
                command.updates().size(),
                command.deleteIds().size()
        );
    }

    @Override
    public List<AutocompleteItem> autocompletePorts(String query, int limit) {
        return portPort.autocomplete(query, limit);
    }
}
