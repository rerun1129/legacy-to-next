package com.freightos.admin.adapter.in.web.attributevalue;

import com.freightos.admin.adapter.in.web.attributevalue.dto.AttributeValueDetailResponse;
import com.freightos.admin.adapter.in.web.attributevalue.dto.AttributeValueSummaryResponse;
import com.freightos.admin.adapter.in.web.attributevalue.dto.CreateAttributeValueRequest;
import com.freightos.admin.adapter.in.web.attributevalue.dto.SaveAttributeValueChangesRequest;
import com.freightos.admin.adapter.in.web.attributevalue.dto.SearchAttributeValueRequest;
import com.freightos.admin.adapter.in.web.attributevalue.dto.UpdateAttributeValueRequest;
import com.freightos.admin.application.attributevalue.port.in.AttributeValueUseCase;
import com.freightos.admin.application.attributevalue.port.in.SaveAttributeValueChangesUseCase;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
import com.freightos.admin.common.request.BulkDeleteByCodeRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
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

@RestController
@RequestMapping("/api/admin/access/attribute-value")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('MENU_ADMIN_ACCESS_ATTRIBUTE')")
public class AttributeValueController {

    private final AttributeValueUseCase attributeValueUseCase;
    private final SaveAttributeValueChangesUseCase saveChangesUseCase;
    private final AttributeValueAssembler attributeValueAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<AttributeValueSummaryResponse>>> search(
            @Valid @RequestBody SearchAttributeValueRequest req) {
        PagedResult<AttributeValueSummary> result = attributeValueUseCase.searchAttributeValues(attributeValueAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(attributeValueAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{attributeKey}/{value}")
    public ResponseEntity<ApiResponse<AttributeValueDetailResponse>> getByKey(
            @PathVariable String attributeKey, @PathVariable String value) {
        return ResponseEntity.ok(ApiResponse.of(attributeValueAssembler.toDetail(attributeValueUseCase.findAttributeValueByKey(attributeKey, value))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody CreateAttributeValueRequest req) {
        attributeValueUseCase.createAttributeValue(attributeValueAssembler.toCreateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.ATTRIBUTE_VALUE_CREATED.getMessage()));
    }

    @PutMapping("/{attributeKey}/{value}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable String attributeKey,
            @PathVariable String value,
            @Valid @RequestBody UpdateAttributeValueRequest req) {
        attributeValueUseCase.updateAttributeValue(attributeKey, value, attributeValueAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.ATTRIBUTE_VALUE_UPDATED.getMessage()));
    }

    @DeleteMapping("/{attributeKey}/{value}")
    public ResponseEntity<ApiResponse<Void>> deleteByKey(
            @PathVariable String attributeKey, @PathVariable String value) {
        attributeValueUseCase.deleteAttributeValueByKey(attributeKey, value);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.ATTRIBUTE_VALUE_DELETED.getMessage()));
    }

    @DeleteMapping("/{attributeKey}/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_ATTRIBUTE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(
            @PathVariable String attributeKey,
            @Valid @RequestBody BulkDeleteByCodeRequest req) {
        attributeValueUseCase.deleteAttributeValues(attributeKey, req.codes());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.ATTRIBUTE_VALUE_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveAttributeValueChangesRequest req) {
        SaveChangesResult result = saveChangesUseCase.saveAttributeValueChanges(attributeValueAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.ATTRIBUTE_VALUE_SAVE_CHANGES.getMessage()));
    }
}
