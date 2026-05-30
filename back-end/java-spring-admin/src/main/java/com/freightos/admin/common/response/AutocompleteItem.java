package com.freightos.admin.common.response;

public record AutocompleteItem(String code, String name, String address) {
    public AutocompleteItem(String code, String name) {
        this(code, name, null);
    }
}
