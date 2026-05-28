package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.common.response.AutocompleteItem;

import java.util.List;

public interface AutocompletePermissionPresetUseCase {
    List<AutocompleteItem> autocompletePermissionPresets(String query, int limit);
}
