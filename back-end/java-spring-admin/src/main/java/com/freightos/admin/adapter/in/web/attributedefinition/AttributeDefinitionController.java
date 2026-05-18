package com.freightos.admin.adapter.in.web.attributedefinition;

import com.freightos.admin.adapter.in.web.attributedefinition.dto.AttributeDefinitionDetailResponse;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.AttributeDefinitionSummaryResponse;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.CreateAttributeDefinitionRequest;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.SearchAttributeDefinitionRequest;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.UpdateAttributeDefinitionRequest;
import com.freightos.admin.application.attributedefinition.port.in.AttributeDefinitionUseCase;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
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
@RequestMapping("/api/admin/access/attribute")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AttributeDefinitionController {

    private final AttributeDefinitionUseCase attributeDefinitionUseCase;
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
}
