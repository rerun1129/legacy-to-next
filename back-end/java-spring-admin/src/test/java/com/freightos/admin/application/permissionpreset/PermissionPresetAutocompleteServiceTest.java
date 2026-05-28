package com.freightos.admin.application.permissionpreset;

import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.common.response.AutocompleteItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PermissionPresetAutocompleteServiceTest {

    @Mock
    private PermissionPresetRepository presetRepository;
    @Mock
    private PermissionPresetAttributeValueRepository presetAttributeValueRepository;
    @Mock
    private UserPermissionPresetRepository userPresetRepository;
    @Mock
    private AttributeValuePort attributeValuePort;

    @InjectMocks
    private PermissionPresetApplicationService service;

    @Test
    void autocompletePermissionPresets_matchingQuery_returnsItems() {
        List<AutocompleteItem> expected = List.of(
                new AutocompleteItem("PRESET_FMS_SEA", "해상 FMS"),
                new AutocompleteItem("PRESET_FMS_AIR", "항공 FMS")
        );
        given(presetRepository.autocompletePermissionPresets("FMS", 20)).willReturn(expected);

        List<AutocompleteItem> result = service.autocompletePermissionPresets("FMS", 20);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("PRESET_FMS_SEA");
        assertThat(result.get(1).code()).isEqualTo("PRESET_FMS_AIR");
        then(presetRepository).should().autocompletePermissionPresets("FMS", 20);
    }

    @Test
    void autocompletePermissionPresets_noMatch_returnsEmptyList() {
        given(presetRepository.autocompletePermissionPresets("NOMATCH", 20)).willReturn(List.of());

        List<AutocompleteItem> result = service.autocompletePermissionPresets("NOMATCH", 20);

        assertThat(result).isEmpty();
    }

    @Test
    void autocompletePermissionPresets_respectsLimitParameter() {
        List<AutocompleteItem> singleResult = List.of(new AutocompleteItem("PRESET_A", "A"));
        given(presetRepository.autocompletePermissionPresets("P", 1)).willReturn(singleResult);

        List<AutocompleteItem> result = service.autocompletePermissionPresets("P", 1);

        assertThat(result).hasSize(1);
        then(presetRepository).should().autocompletePermissionPresets("P", 1);
    }
}
