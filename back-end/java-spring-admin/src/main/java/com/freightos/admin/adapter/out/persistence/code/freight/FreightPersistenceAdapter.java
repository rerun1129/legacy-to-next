package com.freightos.admin.adapter.out.persistence.code.freight;

import com.freightos.admin.application.code.freight.command.SearchFreightCommand;
import com.freightos.admin.application.code.freight.port.out.FreightPort;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.freight.entity.Freight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FreightPersistenceAdapter implements FreightPort {

    private final FreightRepository freightRepository;
    private final FreightDomainToJpaMapper domainToJpaMapper;
    private final FreightJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<FreightSummary> searchSummaries(SearchFreightCommand command) {
        return freightRepository.searchSummaries(command);
    }

    @Override
    public Optional<Freight> findById(Long id) {
        return freightRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Freight freight) {
        FreightJpaEntity entity = domainToJpaMapper.toNewJpa(freight);
        freightRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Freight patchData) {
        FreightJpaEntity entity = freightRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FREIGHT_NOT_FOUND", MessageCode.FREIGHT_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        FreightJpaEntity entity = freightRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FREIGHT_NOT_FOUND", MessageCode.FREIGHT_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return freightRepository.autocomplete(query, limit);
    }
}
