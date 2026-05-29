package com.freightos.admin.adapter.in.web.code.port;

import com.freightos.admin.adapter.in.web.code.port.dto.CreatePortRequest;
import com.freightos.admin.adapter.in.web.code.port.dto.PortDetailResponse;
import com.freightos.admin.adapter.in.web.code.port.dto.PortSummaryResponse;
import com.freightos.admin.adapter.in.web.code.port.dto.SavePortChangesRequest;
import com.freightos.admin.adapter.in.web.code.port.dto.SearchPortRequest;
import com.freightos.admin.adapter.in.web.code.port.dto.UpdatePortRequest;
import com.freightos.admin.application.code.port.port.in.PortUseCase;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.port.entity.Port;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/code/port")
@RequiredArgsConstructor
@Validated
public class PortController {

    private final PortUseCase portUseCase;
    private final PortAssembler portAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_PORT')")
    public ResponseEntity<ApiResponse<PagedResult<PortSummaryResponse>>> search(
            @Valid @RequestBody SearchPortRequest req) {
        PagedResult<PortSummary> result = portUseCase.searchPorts(portAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(portAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_PORT')")
    public ResponseEntity<ApiResponse<PortDetailResponse>> getById(@PathVariable Long id) {
        Port domain = portUseCase.getPortById(id);
        return ResponseEntity.ok(ApiResponse.of(portAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PORT_SAVE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreatePortRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = portUseCase.createPort(portAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/port/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.PORT_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PORT_SAVE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePortRequest req) {
        portUseCase.updatePort(id, portAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PORT_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PORT_SAVE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        portUseCase.deletePort(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PORT_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PORT_SAVE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        portUseCase.deletePorts(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PORT_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_PORT_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SavePortChangesRequest req) {
        SaveChangesResult result = portUseCase.savePortChanges(portAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.PORT_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(portUseCase.autocompletePorts(q, limit)));
    }
}
