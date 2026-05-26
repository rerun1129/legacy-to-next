package com.freightos.admin.adapter.out.persistence.code.packageunit;

import com.freightos.admin.application.code.packageunit.command.SearchPackageUnitCommand;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.response.PagedResult;

public interface PackageUnitRepositoryCustom {
    PagedResult<PackageUnitSummary> searchSummaries(SearchPackageUnitCommand command);
}
