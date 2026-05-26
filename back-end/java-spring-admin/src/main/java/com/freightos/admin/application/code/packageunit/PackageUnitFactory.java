package com.freightos.admin.application.code.packageunit;

import com.freightos.admin.application.code.packageunit.command.CreatePackageUnitCommand;
import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;
import org.springframework.stereotype.Component;

@Component
public class PackageUnitFactory {

    public PackageUnit from(CreatePackageUnitCommand command) {
        return PackageUnit.create(command.packageCode(), command.name(), command.nameEn(), command.active());
    }
}
