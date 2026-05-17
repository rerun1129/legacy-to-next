package com.freightos.admin.adapter.in.web.code;

import com.freightos.admin.adapter.in.web.code.dto.CodeDetailResponse;
import com.freightos.admin.adapter.in.web.code.dto.CodeSummaryResponse;
import com.freightos.admin.adapter.in.web.code.dto.CreateCodeRequest;
import com.freightos.admin.adapter.in.web.code.dto.SearchCodeRequest;
import com.freightos.admin.adapter.in.web.code.dto.UpdateCodeRequest;
import com.freightos.admin.application.code.port.in.CodeUseCase;
import com.freightos.admin.application.code.projection.CodeSummary;
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
@RequestMapping("/api/admin/code")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN') or hasAuthority('CODE_MANAGE')")
public class CodeController {

    private final CodeUseCase codeUseCase;
    private final CodeAssembler codeAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<CodeSummaryResponse>>> search(
            @Valid @RequestBody SearchCodeRequest req) {
        PagedResult<CodeSummary> result = codeUseCase.searchCodes(codeAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(codeAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CodeDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(codeAssembler.toDetail(codeUseCase.findCodeById(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateCodeRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = codeUseCase.createCode(codeAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.CODE_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCodeRequest req) {
        codeUseCase.updateCode(id, codeAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CODE_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        codeUseCase.deleteCodeById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CODE_DELETED.getMessage()));
    }
}
