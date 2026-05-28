package com.freightos.admin.application.permissionpreset;

import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
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
 * attribute_value_id 기반 union 후 AttributeValuePort 로 key/value 변환하여
 * 기존 Map<String, List<String>> 시그니처를 유지한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComputeEffectiveAttributeValuesService {

    private final UserPermissionPresetRepository userPresetRepository;
    private final PermissionPresetRepository presetRepository;
    private final PermissionPresetAttributeValueRepository presetAttributeValueRepository;
    private final AttributeValuePort attributeValuePort;

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
                    .ifPresent(ignored -> mergePresetAttributeValues(result, presetId));
        }
        return result;
    }

    /**
     * preset 의 attribute_value_id 목록을 조회하여 attributeKey→value 로 변환한 뒤 result 맵에 병합한다.
     * 중복 값은 추가하지 않는다.
     */
    private void mergePresetAttributeValues(Map<String, List<String>> result, Long presetId) {
        List<Long> avIds = presetAttributeValueRepository.findAttributeValueIdsByPresetId(presetId);
        if (avIds.isEmpty()) {
            return;
        }
        List<AttributeValue> attributeValues = attributeValuePort.findAttributeValuesByIds(avIds);
        for (AttributeValue av : attributeValues) {
            List<String> existing = result.computeIfAbsent(av.getAttributeKey(), k -> new ArrayList<>());
            if (!existing.contains(av.getValue())) {
                existing.add(av.getValue());
            }
        }
    }
}
