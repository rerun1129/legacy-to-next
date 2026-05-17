package com.freightos.admin.adapter.out.persistence.partner;

import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.port.out.PartnerPort;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.Partner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartnerPersistenceAdapter implements PartnerPort {

    private final PartnerRepository partnerRepository;
    private final PartnerDomainToJpaMapper domainToJpaMapper;
    private final PartnerJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<PartnerSummary> searchSummaries(SearchPartnerCommand command) {
        return partnerRepository.searchSummaries(command);
    }

    @Override
    public Optional<Partner> findById(Long id) {
        return partnerRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Partner partner) {
        PartnerJpaEntity entity = domainToJpaMapper.toNewJpa(partner);
        partnerRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Partner patchData) {
        PartnerJpaEntity entity = partnerRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PARTNER_NOT_FOUND", MessageCode.PARTNER_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        PartnerJpaEntity entity = partnerRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PARTNER_NOT_FOUND", MessageCode.PARTNER_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }
}
