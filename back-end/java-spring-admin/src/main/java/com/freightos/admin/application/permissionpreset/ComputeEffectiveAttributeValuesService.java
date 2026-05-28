package com.freightos.admin.application.permissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.domain.permissionpreset.entity.AttributeValueRef;
import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 유저의 effective attribute 값을 계산하는 헬퍼 서비스.
 *
 * effective = user.attributes (직접 부여분) ∪ (보유한 모든 active preset 의 attribute_value 합집합)
 *
 * Phase 1 에서는 헬퍼만 구현한다. AuthService 통합은 Phase 2 에서 수행한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComputeEffectiveAttributeValuesService {

    private final UserPermissionPresetRepository userPresetRepository;
    private final PermissionPresetRepository presetRepository;
    private final PermissionPresetAttributeValueRepository presetAttributeValueRepository;

    /**
     * userId 와 사용자의 직접 부여 attribute 맵을 받아 effective attribute 맵을 반환한다.
     *
     * @param userId           대상 사용자 PK
     * @param directAttributes user.attributes JSONB 에서 파싱한 직접 부여 맵 (attributeKey → 값 목록)
     * @return effective attribute 맵 (attributeKey → 합집합 값 목록, 중복 제거됨)
     */
    public Map<String, List<String>> computeEffectiveAttributes(Long userId, Map<String, List<String>> directAttributes) {
        Map<String, List<String>> result = new HashMap<>(directAttributes);

        List<Long> presetIds = userPresetRepository.findPresetIdsByUserId(userId);
        if (presetIds.isEmpty()) {
            return result;
        }

        for (Long presetId : presetIds) {
            presetRepository.findPermissionPresetById(presetId)
                    .filter(PermissionPreset::isActive)
                    .ifPresent(ignored -> mergePresetRefs(result, presetId));
        }
        return result;
    }

    /**
     * preset 의 AttributeValueRef 목록을 result 맵에 병합한다.
     * attributeKey → value 로 그룹화하여 중복 없이 추가한다.
     */
    private void mergePresetRefs(Map<String, List<String>> result, Long presetId) {
        List<AttributeValueRef> refs = presetAttributeValueRepository.findAttributeValueRefsByPresetId(presetId);
        for (AttributeValueRef ref : refs) {
            List<String> existing = result.computeIfAbsent(ref.attributeKey(), k -> new ArrayList<>());
            if (!existing.contains(ref.value())) {
                existing.add(ref.value());
            }
        }
    }
}
