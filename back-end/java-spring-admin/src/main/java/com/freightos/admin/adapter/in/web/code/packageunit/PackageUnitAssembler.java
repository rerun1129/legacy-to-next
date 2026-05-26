package com.freightos.admin.adapter.in.web.code.packageunit;

import com.freightos.admin.adapter.in.web.code.packageunit.dto.CreatePackageUnitRequest;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.PackageUnitDetailResponse;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.PackageUnitSummaryResponse;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.SearchPackageUnitRequest;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.UpdatePackageUnitRequest;
import com.freightos.admin.application.code.packageunit.command.CreatePackageUnitCommand;
import com.freightos.admin.application.code.packageunit.command.SearchPackageUnitCommand;
import com.freightos.admin.application.code.packageunit.command.UpdatePackageUnitCommand;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;
import org.springframework.stereotype.Component;

@Component
public class PackageUnitAssembler {

    public SearchPackageUnitCommand toSearchCommand(SearchPackageUnitRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchPackageUnitCommand(req.packageCode(), req.name(), req.scope(), req.page(), size);
    }

    public CreatePackageUnitCommand toCreateCommand(CreatePackageUnitRequest req) {
        return new CreatePackageUnitCommand(req.packageCode(), req.name(), req.nameEn(), req.active());
    }

    public UpdatePackageUnitCommand toUpdateCommand(UpdatePackageUnitRequest req) {
        return new UpdatePackageUnitCommand(req.name(), req.nameEn(), req.active());
    }

    public PackageUnitSummaryResponse toSummaryResponse(PackageUnitSummary p) {
        return new PackageUnitSummaryResponse(p.id(), p.packageCode(), p.name(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public PackageUnitDetailResponse toDetail(PackageUnit domain) {
        return new PackageUnitDetailResponse(
                domain.getId(), domain.getPackageCode(), domain.getName(), domain.getNameEn(),
                domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<PackageUnitSummaryResponse> toSummaryPage(PagedResult<PackageUnitSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
