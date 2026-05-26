package com.freightos.admin.adapter.out.persistence.code.carrier;

import com.freightos.admin.application.code.carrier.command.SearchCarrierCommand;
import com.freightos.admin.application.code.carrier.port.out.CarrierPort;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.carrier.entity.Carrier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CarrierPersistenceAdapter implements CarrierPort {

    private final CarrierRepository carrierRepository;
    private final CarrierDomainToJpaMapper domainToJpaMapper;
    private final CarrierJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<CarrierSummary> searchSummaries(SearchCarrierCommand command) {
        return carrierRepository.searchSummaries(command);
    }

    @Override
    public Optional<Carrier> findById(Long id) {
        return carrierRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Carrier carrier) {
        CarrierJpaEntity entity = domainToJpaMapper.toNewJpa(carrier);
        carrierRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Carrier patchData) {
        CarrierJpaEntity entity = carrierRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CARRIER_NOT_FOUND", MessageCode.CARRIER_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        CarrierJpaEntity entity = carrierRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CARRIER_NOT_FOUND", MessageCode.CARRIER_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }
}
