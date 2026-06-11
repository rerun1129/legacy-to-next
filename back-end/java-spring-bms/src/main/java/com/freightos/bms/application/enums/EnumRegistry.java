package com.freightos.bms.application.enums;

import com.freightos.bms.application.enums.projection.EnumOption;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class EnumRegistry {

    private final Map<String, List<EnumOption>> store;

    private EnumRegistry(Map<String, List<EnumOption>> store) {
        this.store = store;
    }

    public static EnumRegistry of(Map<String, List<EnumOption>> store) {
        return new EnumRegistry(store);
    }

    public Optional<List<EnumOption>> getByName(String name) {
        List<EnumOption> found = store.get(name);
        if (found == null) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(found));
    }

    public Set<String> getAllKeys() {
        return Collections.unmodifiableSet(store.keySet());
    }
}
