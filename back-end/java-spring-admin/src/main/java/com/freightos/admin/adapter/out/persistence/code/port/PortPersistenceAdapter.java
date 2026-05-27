package com.freightos.admin.adapter.out.persistence.code.port;

import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.port.out.PortPort;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.port.entity.Port;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PortPersistenceAdapter implements PortPort {

    private final PortRepository portRepository;
    private final PortDomainToJpaMapper domainToJpaMapper;
    private final PortJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<PortSummary> searchSummaries(SearchPortCommand command) {
        return portRepository.searchSummaries(command);
    }

    @Override
    public Optional<Port> findById(Long id) {
        return portRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Port port) {
        PortJpaEntity entity = domainToJpaMapper.toNewJpa(port);
        portRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Port patchData) {
        PortJpaEntity entity = portRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PORT_NOT_FOUND", MessageCode.PORT_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        PortJpaEntity entity = portRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PORT_NOT_FOUND", MessageCode.PORT_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return portRepository.autocomplete(query, limit);
    }
}
