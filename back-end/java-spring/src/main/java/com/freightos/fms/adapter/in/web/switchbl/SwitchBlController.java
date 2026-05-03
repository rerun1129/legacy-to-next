package com.freightos.fms.adapter.in.web.switchbl;

import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.switchbl.dto.CreateSwitchBlRequest;
import com.freightos.fms.adapter.in.web.switchbl.dto.SwitchBlResponse;
import com.freightos.fms.adapter.in.web.switchbl.dto.UpdateSwitchBlRequest;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.port.in.SwitchBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Tag(name = "Switch B/L", description = "Switch B/L CRUD — E-21/E-22")
@RestController
@RequestMapping("/api/switch-bl")
@RequiredArgsConstructor
@Validated
public class SwitchBlController {

    private final SwitchBlUseCase switchBlUseCase;
    private final SwitchBlAssembler switchBlAssembler;

    @Operation(summary = "Switch B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<SwitchBlResponse>> create(
            @Valid @RequestBody CreateSwitchBlRequest request,
            UriComponentsBuilder uriBuilder) {
        SwitchBl saved = switchBlUseCase.createSwitchBl(switchBlAssembler.toEntity(request));
        URI location = uriBuilder.path("/api/switch-bl/{id}").buildAndExpand(saved.getSwitchBlId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(switchBlAssembler.toResponse(saved), MessageCode.SWITCH_BL_CREATED.message()));
    }

    @Operation(summary = "Switch B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SwitchBlResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(switchBlAssembler.toResponse(switchBlUseCase.findSwitchBlById(id))));
    }

    @Operation(summary = "Switch B/L 조회 by House B/L ID (미존재 시 204 반환)")
    @GetMapping("/by-house-bl/{houseBlId}")
    public ResponseEntity<ApiResponse<SwitchBlResponse>> getByHouseBlId(@PathVariable Long houseBlId) {
        Optional<SwitchBl> found = switchBlUseCase.findOptionalByHouseBlId(houseBlId);
        if (found.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ApiResponse.of(switchBlAssembler.toResponse(found.get())));
    }

    @Operation(summary = "Switch B/L 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SwitchBlResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSwitchBlRequest req) {
        SwitchBl entity = switchBlUseCase.findSwitchBlById(id);
        switchBlAssembler.applyToEntity(req, entity);
        SwitchBl saved = switchBlUseCase.updateSwitchBl(entity);
        return ResponseEntity.ok(ApiResponse.of(switchBlAssembler.toResponse(saved), MessageCode.SWITCH_BL_UPDATED.message()));
    }

    @Operation(summary = "Switch B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        switchBlUseCase.deleteSwitchBl(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.SWITCH_BL_DELETED.message()));
    }
}
