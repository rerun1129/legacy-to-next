package com.freightos.admin.application.permissionpreset;

import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ComputeEffectiveAttributeValuesServiceTest {

    @Mock
    private UserPermissionPresetRepository userPresetRepository;
    @Mock
    private PermissionPresetRepository presetRepository;
    @Mock
    private PermissionPresetAttributeValueRepository presetAttributeValueRepository;
    @Mock
    private AttributeValuePort attributeValuePort;

    @InjectMocks
    private ComputeEffectiveAttributeValuesService service;

    /** 직접 부여 attribute 만 존재하고 preset 없을 때 — 그대로 반환. */
    @Test
    void computeEffective_noPresets_returnDirectAttributesOnly() {
        Long userId = 1L;
        Map<String, List<String>> direct = mapOf("role", List.of("ADMIN"), "module", List.of("fms"));
        given(userPresetRepository.findPresetIdsByUserId(userId)).willReturn(List.of());

        Map<String, List<String>> result = service.computeEffectiveAttributes(userId, direct);

        assertThat(result.get("role")).containsExactly("ADMIN");
        assertThat(result.get("module")).containsExactly("fms");
        // preset 이 없으므로 추가 키 없음
        assertThat(result.keySet()).containsExactlyInAnyOrder("role", "module");
    }

    /** preset 만 보유하고 직접 부여 없을 때 — preset 의 attribute 가 result 에 포함됨. */
    @Test
    void computeEffective_presetOnly_includesPresetAttributeValues() {
        Long userId = 2L;
        Map<String, List<String>> direct = new HashMap<>();
        PermissionPreset activePreset = PermissionPreset.create("PRESET_FMS_SEA", "FMS 해상", null, true);

        AttributeValue roleViewer = AttributeValue.create("role", "VIEWER", "뷰어", 1, true);
        AttributeValue moduleFms = AttributeValue.create("module", "fms", "FMS", 1, true);

        given(userPresetRepository.findPresetIdsByUserId(userId)).willReturn(List.of(10L));
        given(presetRepository.findPermissionPresetById(10L)).willReturn(Optional.of(activePreset));
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(10L)).willReturn(List.of(1L, 2L));
        given(attributeValuePort.findAttributeValuesByIds(List.of(1L, 2L))).willReturn(List.of(roleViewer, moduleFms));

        Map<String, List<String>> result = service.computeEffectiveAttributes(userId, direct);

        assertThat(result.get("role")).containsExactly("VIEWER");
        assertThat(result.get("module")).containsExactly("fms");
    }

    /** 직접 부여 + preset 혼합 — 두 결과가 union 된다. */
    @Test
    void computeEffective_directAndPreset_mergesBoth() {
        Long userId = 3L;
        Map<String, List<String>> direct = mapOf("module", List.of("admin"));
        PermissionPreset activePreset = PermissionPreset.create("PRESET_ADMIN_ALL", "전체 관리자", null, true);

        AttributeValue roleAdmin = AttributeValue.create("role", "ADMIN", "관리자", 1, true);
        AttributeValue moduleFms = AttributeValue.create("module", "fms", "FMS", 1, true);

        given(userPresetRepository.findPresetIdsByUserId(userId)).willReturn(List.of(20L));
        given(presetRepository.findPermissionPresetById(20L)).willReturn(Optional.of(activePreset));
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(20L)).willReturn(List.of(3L, 4L));
        given(attributeValuePort.findAttributeValuesByIds(List.of(3L, 4L))).willReturn(List.of(roleAdmin, moduleFms));

        Map<String, List<String>> result = service.computeEffectiveAttributes(userId, direct);

        // 직접 부여 module=admin + preset module=fms 합산
        assertThat(result.get("module")).containsExactlyInAnyOrder("admin", "fms");
        assertThat(result.get("role")).containsExactly("ADMIN");
    }

    /** deactivate(active=false) preset 은 즉시 effective 에서 제외. */
    @Test
    void computeEffective_inactivePreset_excludedFromResult() {
        Long userId = 4L;
        Map<String, List<String>> direct = new HashMap<>();
        // inactive preset
        PermissionPreset inactivePreset = PermissionPreset.create("PRESET_DEACTIVATED", "비활성 프리셋", null, false);

        given(userPresetRepository.findPresetIdsByUserId(userId)).willReturn(List.of(30L));
        given(presetRepository.findPermissionPresetById(30L)).willReturn(Optional.of(inactivePreset));

        Map<String, List<String>> result = service.computeEffectiveAttributes(userId, direct);

        // inactive 이므로 preset 의 attribute 포함 안 됨
        assertThat(result).isEmpty();
    }

    /** 두 preset 에 동일 값이 있어도 result 에 한 번만 포함됨. */
    @Test
    void computeEffective_duplicateValues_deduplicatedInResult() {
        Long userId = 5L;
        Map<String, List<String>> direct = new HashMap<>();
        PermissionPreset p1 = PermissionPreset.create("PRESET_A", "A", null, true);
        PermissionPreset p2 = PermissionPreset.create("PRESET_B", "B", null, true);

        AttributeValue roleAdmin = AttributeValue.create("role", "ADMIN", "관리자", 1, true);
        AttributeValue moduleFms = AttributeValue.create("module", "fms", "FMS", 1, true);

        given(userPresetRepository.findPresetIdsByUserId(userId)).willReturn(List.of(40L, 50L));
        given(presetRepository.findPermissionPresetById(40L)).willReturn(Optional.of(p1));
        given(presetRepository.findPermissionPresetById(50L)).willReturn(Optional.of(p2));
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(40L)).willReturn(List.of(10L));
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(50L)).willReturn(List.of(10L, 20L));
        given(attributeValuePort.findAttributeValuesByIds(List.of(10L))).willReturn(List.of(roleAdmin));
        given(attributeValuePort.findAttributeValuesByIds(List.of(10L, 20L))).willReturn(List.of(roleAdmin, moduleFms));

        Map<String, List<String>> result = service.computeEffectiveAttributes(userId, direct);

        // role=ADMIN 은 두 preset 에 있지만 중복 없이 한 번만
        assertThat(result.get("role")).containsExactly("ADMIN");
        assertThat(result.get("role")).doesNotHaveDuplicates();
        assertThat(result.get("module")).containsExactly("fms");
    }

    private Map<String, List<String>> mapOf(String k1, List<String> v1, String k2, List<String> v2) {
        Map<String, List<String>> map = new HashMap<>();
        map.put(k1, new ArrayList<>(v1));
        map.put(k2, new ArrayList<>(v2));
        return map;
    }

    private Map<String, List<String>> mapOf(String key, List<String> values) {
        Map<String, List<String>> map = new HashMap<>();
        map.put(key, new ArrayList<>(values));
        return map;
    }
}
