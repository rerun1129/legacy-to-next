package com.freightos.admin.adapter.out.persistence.code.packageunit;

import com.freightos.admin.application.code.packageunit.command.SearchPackageUnitCommand;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface PackageUnitRepositoryCustom {
    PagedResult<PackageUnitSummary> searchSummaries(SearchPackageUnitCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
