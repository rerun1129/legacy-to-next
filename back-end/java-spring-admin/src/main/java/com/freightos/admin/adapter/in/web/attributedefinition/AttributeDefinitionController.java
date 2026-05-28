package com.freightos.admin.adapter.in.web.attributedefinition;

import com.freightos.admin.adapter.in.web.attributedefinition.dto.AttributeDefinitionDetailResponse;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.AttributeDefinitionSummaryResponse;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.CreateAttributeDefinitionRequest;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.ModuleAttributeResponse;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.SaveAttributeDefinitionChangesRequest;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.SearchAttributeDefinitionRequest;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.UpdateAttributeDefinitionRequest;
import com.freightos.admin.application.attributedefinition.ModuleAttributeResult;
import com.freightos.admin.application.attributedefinition.port.in.AttributeDefinitionUseCase;
import com.freightos.admin.application.attributedefinition.port.in.AutocompleteAttributeKeyUseCase;
import com.freightos.admin.application.attributedefinition.port.in.SaveAttributeDefinitionChangesUseCase;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.common.request.BulkDeleteByCodeRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
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
@RequestMapping("/api/admin/access/attribute")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('MENU_ADMIN_ACCESS_ATTRIBUTE')")
public class AttributeDefinitionController {

    private final AttributeDefinitionUseCase attributeDefinitionUseCase;
    private final SaveAttributeDefinitionChangesUseCase saveChangesUseCase;
    private final AutocompleteAttributeKeyUseCase autocompleteUseCase;
    private final AttributeDefinitionAssembler attributeDefinitionAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<AttributeDefinitionSummaryResponse>>> search(
            @Valid @RequestBody SearchAttributeDefinitionRequest req) {
        PagedResult<AttributeDefinitionSummary> result = attributeDefinitionUseCase.searchAttributeDefinitions(attributeDefinitionAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(attributeDefinitionAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{attributeKey}")
    public ResponseEntity<ApiResponse<AttributeDefinitionDetailResponse>> getByKey(@PathVariable String attributeKey) {
        return ResponseEntity.ok(ApiResponse.of(attributeDefinitionAssembler.toDetail(attributeDefinitionUseCase.findAttributeDefinitionByKey(attributeKey))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> create(
            @Valid @RequestBody CreateAttributeDefinitionRequest req,
            UriComponentsBuilder uriBuilder) {
        String attributeKey = attributeDefinitionUseCase.createAttributeDefinition(attributeDefinitionAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/access/attribute/{attributeKey}").buildAndExpand(attributeKey).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("attributeKey", attributeKey), MessageCode.ATTRIBUTE_DEFINITION_CREATED.getMessage()));
    }

    @PutMapping("/{attributeKey}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable String attributeKey,
            @Valid @RequestBody UpdateAttributeDefinitionRequest req) {
        attributeDefinitionUseCase.updateAttributeDefinition(attributeKey, attributeDefinitionAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.ATTRIBUTE_DEFINITION_UPDATED.getMessage()));
    }

    @DeleteMapping("/{attributeKey}")
    public ResponseEntity<ApiResponse<Void>> deleteByKey(@PathVariable String attributeKey) {
        attributeDefinitionUseCase.deleteAttributeDefinitionByKey(attributeKey);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.ATTRIBUTE_DEFINITION_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_ATTRIBUTE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteByCodeRequest req) {
        attributeDefinitionUseCase.deleteAttributeDefinitionsByKeys(req.codes());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.ATTRIBUTE_DEFINITION_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveAttributeDefinitionChangesRequest req) {
        SaveChangesResult result = saveChangesUseCase.saveAttributeDefinitionChanges(attributeDefinitionAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.ATTRIBUTE_DEFINITION_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(autocompleteUseCase.autocompleteAttributeKeys(query, limit)));
    }

    @GetMapping("/by-module/{moduleCode}")
    public ResponseEntity<ApiResponse<List<ModuleAttributeResponse>>> getByModule(@PathVariable String moduleCode) {
        List<ModuleAttributeResult> results = attributeDefinitionUseCase.findAttributesByModuleCode(moduleCode);
        return ResponseEntity.ok(ApiResponse.of(attributeDefinitionAssembler.toModuleAttributeResponseList(results)));
    }
}
