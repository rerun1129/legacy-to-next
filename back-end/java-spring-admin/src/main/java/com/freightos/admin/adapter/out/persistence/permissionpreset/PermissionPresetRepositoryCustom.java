package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.common.response.AutocompleteItem;

import java.util.List;

public interface PermissionPresetRepositoryCustom {
    List<AutocompleteItem> autocompletePermissionPresets(String query, int limit);
}
