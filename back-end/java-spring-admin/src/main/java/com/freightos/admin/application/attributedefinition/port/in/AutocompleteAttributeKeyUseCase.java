package com.freightos.admin.application.attributedefinition.port.in;

import com.freightos.admin.common.response.AutocompleteItem;

import java.util.List;

public interface AutocompleteAttributeKeyUseCase {
    List<AutocompleteItem> autocompleteAttributeKeys(String query, int limit);
}
