package com.freightos.admin.application.code.country.command;

public record SearchCountryCommand(
        String countryCode,
        String name,
        String scope,
        int page,
        int size
) {}
