package com.freightos.admin.adapter.in.web.codemaster;

import com.freightos.admin.adapter.in.web.codemaster.dto.CodeMasterDetailResponse;
import com.freightos.admin.adapter.in.web.codemaster.dto.CodeMasterSummaryResponse;
import com.freightos.admin.adapter.in.web.codemaster.dto.CreateCodeMasterRequest;
import com.freightos.admin.adapter.in.web.codemaster.dto.SearchCodeMasterRequest;
import com.freightos.admin.adapter.in.web.codemaster.dto.UpdateCodeMasterRequest;
import com.freightos.admin.application.codemaster.port.in.CodeMasterUseCase;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
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
@RequestMapping("/api/admin/code-master")
@RequiredArgsConstructor
@Validated
public class CodeMasterController {

    private final CodeMasterUseCase codeMasterUseCase;
    private final CodeMasterAssembler codeMasterAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_LIST')")
    public ResponseEntity<ApiResponse<PagedResult<CodeMasterSummaryResponse>>> search(
            @Valid @RequestBody SearchCodeMasterRequest req) {
        PagedResult<CodeMasterSummary> result = codeMasterUseCase.searchCodeMasters(codeMasterAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(codeMasterAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_LIST')")
    public ResponseEntity<ApiResponse<CodeMasterDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(codeMasterAssembler.toDetail(codeMasterUseCase.findCodeMasterById(id))));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_LIST_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateCodeMasterRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = codeMasterUseCase.createCodeMaster(codeMasterAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code-master/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.CODE_MASTER_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_LIST_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCodeMasterRequest req) {
        codeMasterUseCase.updateCodeMaster(id, codeMasterAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CODE_MASTER_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_LIST_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        codeMasterUseCase.deleteCodeMasterById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CODE_MASTER_DELETED.getMessage()));
    }
}
