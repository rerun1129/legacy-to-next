package com.freightos.admin.adapter.in.web.module;

import com.freightos.admin.adapter.in.web.module.dto.CreateModuleRequest;
import com.freightos.admin.adapter.in.web.module.dto.ModuleDetailResponse;
import com.freightos.admin.adapter.in.web.module.dto.ModuleSummaryResponse;
import com.freightos.admin.adapter.in.web.module.dto.SearchModuleRequest;
import com.freightos.admin.adapter.in.web.module.dto.UpdateModuleRequest;
import com.freightos.admin.application.module.port.in.ModuleUseCase;
import com.freightos.admin.application.module.projection.ModuleSummary;
import com.freightos.admin.common.request.BulkDeleteByCodeRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
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
@RequestMapping("/api/admin/access/module")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class ModuleController {

    private final ModuleUseCase moduleUseCase;
    private final ModuleAssembler moduleAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<ModuleSummaryResponse>>> search(
            @Valid @RequestBody SearchModuleRequest req) {
        PagedResult<ModuleSummary> result = moduleUseCase.searchModules(moduleAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(moduleAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{moduleCode}")
    public ResponseEntity<ApiResponse<ModuleDetailResponse>> getByCode(@PathVariable String moduleCode) {
        return ResponseEntity.ok(ApiResponse.of(moduleAssembler.toDetail(moduleUseCase.findModuleByCode(moduleCode))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> create(
            @Valid @RequestBody CreateModuleRequest req,
            UriComponentsBuilder uriBuilder) {
        String moduleCode = moduleUseCase.createModule(moduleAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/access/module/{moduleCode}").buildAndExpand(moduleCode).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("moduleCode", moduleCode), MessageCode.MODULE_CREATED.getMessage()));
    }

    @PutMapping("/{moduleCode}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable String moduleCode,
            @Valid @RequestBody UpdateModuleRequest req) {
        moduleUseCase.updateModule(moduleCode, moduleAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MODULE_UPDATED.getMessage()));
    }

    @DeleteMapping("/{moduleCode}")
    public ResponseEntity<ApiResponse<Void>> deleteByCode(@PathVariable String moduleCode) {
        moduleUseCase.deleteModuleByCode(moduleCode);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MODULE_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_MODULE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteByCodeRequest req) {
        moduleUseCase.deleteModulesByCodes(req.codes());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MODULE_DELETED.getMessage()));
    }
}
