package com.freightos.admin.application.permissionpreset;

import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.permissionpreset.command.AssignAttributeValuesCommand;
import com.freightos.admin.application.permissionpreset.command.CreatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.ListPermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.SavePermissionPresetChangesCommand;
import com.freightos.admin.application.permissionpreset.command.UpdatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.port.in.AssignAttributeValuesToPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.AutocompletePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.CreatePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.DeletePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.GetPermissionPresetDetailUseCase;
import com.freightos.admin.application.permissionpreset.port.in.GetPermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.ListPermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.SavePermissionPresetChangesUseCase;
import com.freightos.admin.application.permissionpreset.port.in.UpdatePermissionPresetUseCase;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetDetail;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionPresetApplicationService
        implements CreatePermissionPresetUseCase,
                   UpdatePermissionPresetUseCase,
                   DeletePermissionPresetUseCase,
                   GetPermissionPresetUseCase,
                   GetPermissionPresetDetailUseCase,
                   ListPermissionPresetUseCase,
                   AssignAttributeValuesToPresetUseCase,
                   SavePermissionPresetChangesUseCase,
                   AutocompletePermissionPresetUseCase {

    private final PermissionPresetRepository presetRepository;
    private final PermissionPresetAttributeValueRepository presetAttributeValueRepository;
    private final UserPermissionPresetRepository userPresetRepository;
    private final AttributeValuePort attributeValuePort;

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
    public PermissionPresetDetail getPermissionPresetDetail(Long presetId) {
        PermissionPreset preset = getPermissionPresetById(presetId);
        List<Long> avIds = preset.getAttributeValueIds();
        List<AttributeValue> avs = avIds.isEmpty() ? List.of() : attributeValuePort.findAttributeValuesByIds(avIds);
        List<PermissionPresetDetail.AttributeValueItem> items = avs.stream()
                .map(av -> new PermissionPresetDetail.AttributeValueItem(av.getId(), av.getAttributeKey(), av.getValue(), av.getLabel()))
                .toList();
        return new PermissionPresetDetail(preset.getId(), preset.getCode(), preset.getName(), preset.getDescription(), preset.isActive(), avIds, items);
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
    public SaveChangesResult savePermissionPresetChanges(SavePermissionPresetChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deletePermissionPreset(id);
        }
        for (SavePermissionPresetChangesCommand.UpdatePermissionPresetItem item : command.updates()) {
            updatePermissionPreset(item.id(), new UpdatePermissionPresetCommand(item.name(), item.description(), item.active()));
        }
        for (CreatePermissionPresetCommand create : command.creates()) {
            createPermissionPreset(create);
        }
        return new SaveChangesResult(command.creates().size(), command.updates().size(), command.deleteIds().size());
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
        // 변경 후 최종 attribute_value set 이 비어있지 않으면 module + scope 조합 필수 검증
        validateModuleAndScopeIfNotEmpty(presetId, command);
    }

    @Override
    public List<AutocompleteItem> autocompletePermissionPresets(String query, int limit) {
        return presetRepository.autocompletePermissionPresets(query, limit);
    }

    /**
     * remove/add 적용 후 preset 의 attribute_value 결과 set 이 비어있지 않을 때,
     * module 키 ≥ 1 AND (admin_scope 키 OR fms_scope 키) ≥ 1 을 강제한다.
     * 비즈니스 규칙: module 없는 scope, scope 없는 module 은 ABAC 접근 제어 미완성 상태.
     */
    private void validateModuleAndScopeIfNotEmpty(Long presetId, AssignAttributeValuesCommand command) {
        Set<Long> currentIds = new HashSet<>(presetAttributeValueRepository.findAttributeValueIdsByPresetId(presetId));
        currentIds.removeAll(command.removeIds());
        currentIds.addAll(command.addIds());
        if (currentIds.isEmpty()) {
            return;
        }
        List<AttributeValue> values = attributeValuePort.findAttributeValuesByIds(currentIds);
        boolean hasModule     = values.stream().anyMatch(av -> "module".equals(av.getAttributeKey()));
        boolean hasScope      = values.stream().anyMatch(av -> "admin_scope".equals(av.getAttributeKey()) || "fms_scope".equals(av.getAttributeKey()));
        if (!hasModule || !hasScope) {
            throw ApplicationException.badRequest(
                    "PERMISSION_PRESET_REQUIRES_MODULE_AND_SCOPE",
                    MessageCode.PERMISSION_PRESET_REQUIRES_MODULE_AND_SCOPE.getMessage()
            );
        }
    }
}
