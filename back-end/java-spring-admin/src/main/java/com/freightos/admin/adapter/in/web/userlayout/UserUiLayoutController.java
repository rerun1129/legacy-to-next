package com.freightos.admin.adapter.in.web.userlayout;

import com.fasterxml.jackson.databind.JsonNode;
import com.freightos.admin.adapter.in.web.userlayout.dto.SaveLayoutRequest;
import com.freightos.admin.application.userlayout.port.in.UserUiLayoutUseCase;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/ui-layout")
@RequiredArgsConstructor
@Validated
public class UserUiLayoutController {

    private final UserUiLayoutUseCase userUiLayoutUseCase;
    private final UserUiLayoutAssembler userUiLayoutAssembler;

    @GetMapping("/{storageKey}")
    public ResponseEntity<ApiResponse<JsonNode>> getLayout(@PathVariable String storageKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<String> payloadOpt = userUiLayoutUseCase.getLayout(auth.getName(), storageKey);
        if (payloadOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.of(null));
        }
        JsonNode jsonNode = userUiLayoutAssembler.toJsonNode(payloadOpt.get());
        return ResponseEntity.ok(ApiResponse.of(jsonNode));
    }

    @PutMapping("/{storageKey}")
    public ResponseEntity<ApiResponse<Void>> saveLayout(
            @PathVariable String storageKey,
            @Valid @RequestBody SaveLayoutRequest request) {
        // Jackson은 JSON null 리터럴을 NullNode(non-null 객체)로 역직렬화하므로 @NotNull만으로 거를 수 없음.
        if (request.payload() == null || request.payload().isNull()) {
            throw ApplicationException.badRequest("UI_LAYOUT_PAYLOAD_REQUIRED", "payload는 null이 될 수 없습니다.");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String payload = userUiLayoutAssembler.toPayloadString(request);
        userUiLayoutUseCase.saveLayout(auth.getName(), storageKey, payload);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.UI_LAYOUT_SAVED.getMessage()));
    }

    @DeleteMapping("/{storageKey}")
    public ResponseEntity<ApiResponse<Void>> deleteLayout(@PathVariable String storageKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userUiLayoutUseCase.deleteLayout(auth.getName(), storageKey);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.UI_LAYOUT_DELETED.getMessage()));
    }
}
