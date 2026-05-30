package com.freightos.admin.adapter.in.web.code.carrier;

import com.freightos.admin.adapter.in.web.code.carrier.dto.CarrierDetailResponse;
import com.freightos.admin.adapter.in.web.code.carrier.dto.CarrierSummaryResponse;
import com.freightos.admin.adapter.in.web.code.carrier.dto.CreateCarrierRequest;
import com.freightos.admin.adapter.in.web.code.carrier.dto.SaveCarrierChangesRequest;
import com.freightos.admin.adapter.in.web.code.carrier.dto.SearchCarrierRequest;
import com.freightos.admin.adapter.in.web.code.carrier.dto.UpdateCarrierRequest;
import com.freightos.admin.application.code.carrier.port.in.CarrierUseCase;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.carrier.entity.Carrier;
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
@RequestMapping("/api/admin/code/carrier")
@RequiredArgsConstructor
@Validated
public class CarrierController {

    private final CarrierUseCase carrierUseCase;
    private final CarrierAssembler carrierAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_CARRIER')")
    public ResponseEntity<ApiResponse<PagedResult<CarrierSummaryResponse>>> search(
            @Valid @RequestBody SearchCarrierRequest req) {
        PagedResult<CarrierSummary> result = carrierUseCase.searchCarriers(carrierAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(carrierAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_CARRIER')")
    public ResponseEntity<ApiResponse<CarrierDetailResponse>> getById(@PathVariable Long id) {
        Carrier domain = carrierUseCase.getCarrierById(id);
        return ResponseEntity.ok(ApiResponse.of(carrierAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CARRIER_SAVE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateCarrierRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = carrierUseCase.createCarrier(carrierAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/carrier/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.CARRIER_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CARRIER_SAVE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarrierRequest req) {
        carrierUseCase.updateCarrier(id, carrierAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CARRIER_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CARRIER_SAVE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        carrierUseCase.deleteCarrier(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CARRIER_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CARRIER_SAVE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        carrierUseCase.deleteCarriers(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CARRIER_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CARRIER_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveCarrierChangesRequest req) {
        SaveChangesResult result = carrierUseCase.saveCarrierChanges(carrierAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.CARRIER_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(carrierUseCase.autocompleteCarriers(q, type, limit)));
    }
}
