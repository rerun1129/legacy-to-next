package com.freightos.admin.adapter.out.persistence.code.packageunit;

import com.freightos.admin.application.code.packageunit.command.SearchPackageUnitCommand;
import com.freightos.admin.application.code.packageunit.port.out.PackageUnitPort;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PackageUnitPersistenceAdapter implements PackageUnitPort {

    private final PackageUnitRepository packageUnitRepository;
    private final PackageUnitDomainToJpaMapper domainToJpaMapper;
    private final PackageUnitJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<PackageUnitSummary> searchSummaries(SearchPackageUnitCommand command) {
        return packageUnitRepository.searchSummaries(command);
    }

    @Override
    public Optional<PackageUnit> findById(Long id) {
        return packageUnitRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(PackageUnit packageUnit) {
        PackageUnitJpaEntity entity = domainToJpaMapper.toNewJpa(packageUnit);
        packageUnitRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, PackageUnit patchData) {
        PackageUnitJpaEntity entity = packageUnitRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PACKAGE_UNIT_NOT_FOUND", MessageCode.PACKAGE_UNIT_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        PackageUnitJpaEntity entity = packageUnitRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PACKAGE_UNIT_NOT_FOUND", MessageCode.PACKAGE_UNIT_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return packageUnitRepository.autocomplete(query, limit);
    }
}
