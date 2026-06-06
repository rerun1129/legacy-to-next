package com.freightos.pms.application.enums;

import com.freightos.pms.application.enums.projection.EnumOption;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class EnumRegistry {

    private final Map<String, List<EnumOption>> store;
    private final String etag;

    private EnumRegistry(Map<String, List<EnumOption>> store) {
        this.store = store;
        this.etag = computeEtag(store.keySet());
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

    public String getEtag() {
        return etag;
    }

    private static String computeEtag(Set<String> keys) {
        String joined = keys.stream()
                .sorted()
                .collect(Collectors.joining(","));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
