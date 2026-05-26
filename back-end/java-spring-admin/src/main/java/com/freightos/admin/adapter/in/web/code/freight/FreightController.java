package com.freightos.admin.adapter.in.web.code.freight;

import com.freightos.admin.adapter.in.web.code.freight.dto.CreateFreightRequest;
import com.freightos.admin.adapter.in.web.code.freight.dto.FreightDetailResponse;
import com.freightos.admin.adapter.in.web.code.freight.dto.FreightSummaryResponse;
import com.freightos.admin.adapter.in.web.code.freight.dto.SearchFreightRequest;
import com.freightos.admin.adapter.in.web.code.freight.dto.UpdateFreightRequest;
import com.freightos.admin.application.code.freight.port.in.FreightUseCase;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.freight.entity.Freight;
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
@RequestMapping("/api/admin/code/freight")
@RequiredArgsConstructor
@Validated
public class FreightController {

    private final FreightUseCase freightUseCase;
    private final FreightAssembler freightAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_FREIGHT')")
    public ResponseEntity<ApiResponse<PagedResult<FreightSummaryResponse>>> search(
            @Valid @RequestBody SearchFreightRequest req) {
        PagedResult<FreightSummary> result = freightUseCase.searchFreights(freightAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(freightAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_FREIGHT')")
    public ResponseEntity<ApiResponse<FreightDetailResponse>> getById(@PathVariable Long id) {
        Freight domain = freightUseCase.getFreightById(id);
        return ResponseEntity.ok(ApiResponse.of(freightAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_FREIGHT_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateFreightRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = freightUseCase.createFreight(freightAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/freight/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.FREIGHT_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_FREIGHT_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFreightRequest req) {
        freightUseCase.updateFreight(id, freightAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.FREIGHT_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_FREIGHT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        freightUseCase.deleteFreight(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.FREIGHT_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_FREIGHT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        freightUseCase.deleteFreights(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.FREIGHT_DELETED.getMessage()));
    }
}
