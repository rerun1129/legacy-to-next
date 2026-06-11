package com.freightos.admin.adapter.in.web.commoncode;

import com.freightos.admin.adapter.in.web.commoncode.dto.CommonCodeGroupResponse;
import com.freightos.admin.adapter.in.web.commoncode.dto.CommonCodeResponse;
import com.freightos.admin.adapter.in.web.commoncode.dto.SaveCommonCodeChangesRequest;
import com.freightos.admin.application.commoncode.port.in.CommonCodeUseCase;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.SaveChangesResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/common-code")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('MENU_ADMIN_COMMON_CODE')")
public class CommonCodeController {

    private final CommonCodeUseCase commonCodeUseCase;
    private final CommonCodeAssembler commonCodeAssembler;

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<CommonCodeGroupResponse>>> getGroups() {
        List<CommonCodeGroupResponse> result =
                commonCodeAssembler.toGroupResponseList(commonCodeUseCase.getCommonCodeGroups());
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommonCodeResponse>>> getCodesByGroup(
            @RequestParam String group) {
        List<CommonCodeResponse> result =
                commonCodeAssembler.toCodeResponseList(commonCodeUseCase.getCommonCodesByGroup(group));
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_COMMON_CODE_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveCommonCodeChangesRequest req) {
        SaveChangesResult result =
                commonCodeUseCase.saveCommonCodeChanges(commonCodeAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.COMMON_CODE_SAVE_CHANGES.getMessage()));
    }
}
