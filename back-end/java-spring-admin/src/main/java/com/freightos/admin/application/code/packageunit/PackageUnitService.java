package com.freightos.admin.application.code.packageunit;

import com.freightos.admin.application.code.packageunit.command.CreatePackageUnitCommand;
import com.freightos.admin.application.code.packageunit.command.SearchPackageUnitCommand;
import com.freightos.admin.application.code.packageunit.command.UpdatePackageUnitCommand;
import com.freightos.admin.application.code.packageunit.port.in.PackageUnitUseCase;
import com.freightos.admin.application.code.packageunit.port.out.PackageUnitPort;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PackageUnitService implements PackageUnitUseCase {

    private final PackageUnitPort packageUnitPort;
    private final PackageUnitFactory packageUnitFactory;

    @Override
    public PagedResult<PackageUnitSummary> searchPackageUnits(SearchPackageUnitCommand command) {
        return packageUnitPort.searchSummaries(command);
    }

    @Override
    public PackageUnit getPackageUnitById(Long id) {
        return packageUnitPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("PACKAGE_UNIT_NOT_FOUND", MessageCode.PACKAGE_UNIT_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createPackageUnit(CreatePackageUnitCommand command) {
        try {
            return packageUnitPort.save(packageUnitFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("PACKAGE_UNIT_DUPLICATE_CODE", MessageCode.PACKAGE_UNIT_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updatePackageUnit(Long id, UpdatePackageUnitCommand command) {
        PackageUnit packageUnit = getPackageUnitById(id);
        if (packageUnit.isDeleted()) {
            throw ApplicationException.conflict("PACKAGE_UNIT_ALREADY_DELETED", MessageCode.PACKAGE_UNIT_ALREADY_DELETED.getMessage());
        }
        packageUnit.applyUpdate(command.name(), command.nameEn(), command.active());
        packageUnitPort.update(id, packageUnit);
    }

    @Override
    @Transactional
    public void deletePackageUnit(Long id) {
        PackageUnit packageUnit = getPackageUnitById(id);
        if (packageUnit.isDeleted()) {
            throw ApplicationException.conflict("PACKAGE_UNIT_ALREADY_DELETED", MessageCode.PACKAGE_UNIT_ALREADY_DELETED.getMessage());
        }
        packageUnitPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deletePackageUnits(List<Long> ids) {
        for (Long id : ids) {
            deletePackageUnit(id);
        }
    }
}
