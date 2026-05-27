package com.freightos.admin.application.code.packageunit.port.in;

import com.freightos.admin.application.code.packageunit.command.CreatePackageUnitCommand;
import com.freightos.admin.application.code.packageunit.command.SavePackageUnitChangesCommand;
import com.freightos.admin.application.code.packageunit.command.SearchPackageUnitCommand;
import com.freightos.admin.application.code.packageunit.command.UpdatePackageUnitCommand;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;

import java.util.List;

public interface PackageUnitUseCase {
    PagedResult<PackageUnitSummary> searchPackageUnits(SearchPackageUnitCommand command);
    PackageUnit getPackageUnitById(Long id);
    Long createPackageUnit(CreatePackageUnitCommand command);
    void updatePackageUnit(Long id, UpdatePackageUnitCommand command);
    void deletePackageUnit(Long id);
    void deletePackageUnits(List<Long> ids);
    SaveChangesResult savePackageUnitChanges(SavePackageUnitChangesCommand command);
    List<AutocompleteItem> autocompletePackageUnits(String query, int limit);
}
