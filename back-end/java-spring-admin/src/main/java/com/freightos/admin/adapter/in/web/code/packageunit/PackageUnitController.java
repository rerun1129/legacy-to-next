package com.freightos.admin.adapter.in.web.code.packageunit;

import com.freightos.admin.adapter.in.web.code.packageunit.dto.CreatePackageUnitRequest;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.PackageUnitDetailResponse;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.PackageUnitSummaryResponse;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.SearchPackageUnitRequest;
import com.freightos.admin.adapter.in.web.code.packageunit.dto.UpdatePackageUnitRequest;
import com.freightos.admin.application.code.packageunit.port.in.PackageUnitUseCase;
import com.freightos.admin.application.code.packageunit.projection.PackageUnitSummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/code/package")
@RequiredArgsConstructor
@Validated
public class PackageUnitController {

    private final PackageUnitUseCase packageUnitUseCase;
    private final PackageUnitAssembler packageUnitAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_PACKAGE')")
    public ResponseEntity<ApiResponse<PagedResult<PackageUnitSummaryResponse>>> search(
            @Valid @RequestBody SearchPackageUnitRequest req) {
        PagedResult<PackageUnitSummary> result = packageUnitUseCase.searchPackageUnits(packageUnitAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(packageUnitAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_PACKAGE')")
    public ResponseEntity<ApiResponse<PackageUnitDetailResponse>> getById(@PathVariable Long id) {
        PackageUnit domain = packageUnitUseCase.getPackageUnitById(id);
        return ResponseEntity.ok(ApiResponse.of(packageUnitAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PACKAGE_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreatePackageUnitRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = packageUnitUseCase.createPackageUnit(packageUnitAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/package/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.PACKAGE_UNIT_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PACKAGE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePackageUnitRequest req) {
        packageUnitUseCase.updatePackageUnit(id, packageUnitAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PACKAGE_UNIT_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PACKAGE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        packageUnitUseCase.deletePackageUnit(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PACKAGE_UNIT_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PACKAGE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        packageUnitUseCase.deletePackageUnits(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PACKAGE_UNIT_DELETED.getMessage()));
    }
}
