package com.freightos.admin.adapter.in.web.codedetail;

import com.freightos.admin.adapter.in.web.codedetail.dto.CodeDetailDetailResponse;
import com.freightos.admin.adapter.in.web.codedetail.dto.CodeDetailSummaryResponse;
import com.freightos.admin.adapter.in.web.codedetail.dto.CreateCodeDetailRequest;
import com.freightos.admin.adapter.in.web.codedetail.dto.SearchCodeDetailRequest;
import com.freightos.admin.adapter.in.web.codedetail.dto.UpdateCodeDetailRequest;
import com.freightos.admin.application.codedetail.port.in.CodeDetailUseCase;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
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
@RequestMapping("/api/admin/code-detail")
@RequiredArgsConstructor
@Validated
public class CodeDetailController {

    private final CodeDetailUseCase codeDetailUseCase;
    private final CodeDetailAssembler codeDetailAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_LIST')")
    public ResponseEntity<ApiResponse<PagedResult<CodeDetailSummaryResponse>>> search(
            @Valid @RequestBody SearchCodeDetailRequest req) {
        PagedResult<CodeDetailSummary> result = codeDetailUseCase.searchCodeDetails(codeDetailAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(codeDetailAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_LIST')")
    public ResponseEntity<ApiResponse<CodeDetailDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(codeDetailAssembler.toDetail(codeDetailUseCase.findCodeDetailById(id))));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_LIST_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateCodeDetailRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = codeDetailUseCase.createCodeDetail(codeDetailAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code-detail/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.CODE_DETAIL_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_LIST_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCodeDetailRequest req) {
        codeDetailUseCase.updateCodeDetail(id, codeDetailAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CODE_DETAIL_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_LIST_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        codeDetailUseCase.deleteCodeDetailById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CODE_DETAIL_DELETED.getMessage()));
    }
}
