package com.freightos.admin.application.code.country.command;

public record UpdateCountryCommand(
        String name,
        String nameEn,
        boolean active
) {}
