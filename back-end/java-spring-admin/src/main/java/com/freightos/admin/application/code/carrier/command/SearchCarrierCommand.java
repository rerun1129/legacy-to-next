package com.freightos.admin.application.code.carrier.command;

public record SearchCarrierCommand(
        String carrierCode,
        String name,
        String carrierType,
        String scope,
        int page,
        int size
) {}
