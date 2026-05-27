package com.freightos.admin.application.code.country.command;

public record CreateCountryCommand(
        String countryCode,
        String name,
        String nameEn,
        boolean active
) {}
