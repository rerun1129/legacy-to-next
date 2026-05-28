package com.freightos.admin.application.permissionpreset;

import com.freightos.admin.application.permissionpreset.command.AssignAttributeValuesCommand;
import com.freightos.admin.application.permissionpreset.command.CreatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.ListPermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.UpdatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.port.in.AssignAttributeValuesToPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.CreatePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.DeletePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.GetPermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.ListPermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.UpdatePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionPresetApplicationService
        implements CreatePermissionPresetUseCase,
                   UpdatePermissionPresetUseCase,
                   DeletePermissionPresetUseCase,
                   GetPermissionPresetUseCase,
                   ListPermissionPresetUseCase,
                   AssignAttributeValuesToPresetUseCase {

    private final PermissionPresetRepository presetRepository;
    private final PermissionPresetAttributeValueRepository presetAttributeValueRepository;
    private final UserPermissionPresetRepository userPresetRepository;

    @Override
    @Transactional
    public Long createPermissionPreset(CreatePermissionPresetCommand command) {
        if (presetRepository.existsPermissionPresetByCode(command.code())) {
            throw ApplicationException.conflict("PERMISSION_PRESET_CODE_DUPLICATE", MessageCode.PERMISSION_PRESET_CODE_DUPLICATE.getMessage());
        }
        PermissionPreset preset = PermissionPreset.create(command.code(), command.name(), command.description(), command.active());
        return presetRepository.savePermissionPreset(preset);
    }

    @Override
    @Transactional
    public void updatePermissionPreset(Long presetId, UpdatePermissionPresetCommand command) {
        PermissionPreset existing = getPermissionPresetById(presetId);
        existing.applyUpdate(command.name(), command.description(), command.active());
        presetRepository.updatePermissionPreset(presetId, existing);
    }

    @Override
    @Transactional
    public void deletePermissionPreset(Long presetId) {
        if (!presetRepository.existsPermissionPresetById(presetId)) {
            throw ApplicationException.notFound("PERMISSION_PRESET_NOT_FOUND", MessageCode.PERMISSION_PRESET_NOT_FOUND.getMessage());
        }
        // RESTRICT: 부여된 user 가 존재하면 삭제 거부
        if (userPresetRepository.countByPresetId(presetId) > 0) {
            throw ApplicationException.conflict("PERMISSION_PRESET_IN_USE_CANNOT_DELETE", MessageCode.PERMISSION_PRESET_IN_USE_CANNOT_DELETE.getMessage());
        }
        presetRepository.deletePermissionPresetById(presetId);
    }

    @Override
    public PermissionPreset getPermissionPresetById(Long presetId) {
        return presetRepository.findPermissionPresetById(presetId)
                .orElseThrow(() -> ApplicationException.notFound("PERMISSION_PRESET_NOT_FOUND", MessageCode.PERMISSION_PRESET_NOT_FOUND.getMessage()));
    }

    @Override
    public List<PermissionPresetSummary> listPermissionPresets(ListPermissionPresetCommand command) {
        return presetRepository.findAllPermissionPresets(command.activeOnly())
                .stream()
                .map(preset -> new PermissionPresetSummary(
                        preset.getId(),
                        preset.getCode(),
                        preset.getName(),
                        preset.getDescription(),
                        preset.isActive(),
                        preset.getAttributeValueIds()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void assignAttributeValuesToPreset(Long presetId, AssignAttributeValuesCommand command) {
        if (!presetRepository.existsPermissionPresetById(presetId)) {
            throw ApplicationException.notFound("PERMISSION_PRESET_NOT_FOUND", MessageCode.PERMISSION_PRESET_NOT_FOUND.getMessage());
        }
        // remove 먼저 처리
        if (!command.removeIds().isEmpty()) {
            presetAttributeValueRepository.deleteByPresetIdAndAttributeValueIdsIn(presetId, command.removeIds());
        }
        if (!command.addIds().isEmpty()) {
            presetAttributeValueRepository.saveAllByPresetId(presetId, command.addIds());
        }
    }
}
