package com.freightos.admin.application.code.packageunit.port.out;

import com.freightos.admin.application.code.packageunit.command.SearchPackageUnitCommand;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;

import java.util.Optional;

public interface PackageUnitPort {
    PagedResult<PackageUnitSummary> searchSummaries(SearchPackageUnitCommand command);
    Optional<PackageUnit> findById(Long id);
    Long save(PackageUnit packageUnit);
    void update(Long id, PackageUnit patchData);
    void softDelete(Long id);
}
