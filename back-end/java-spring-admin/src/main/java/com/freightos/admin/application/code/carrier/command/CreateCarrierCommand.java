package com.freightos.admin.application.code.carrier.command;

import com.freightos.admin.domain.code.carrier.entity.CarrierType;

public record CreateCarrierCommand(
        String carrierCode,
        String name,
        String nameEn,
        CarrierType carrierType,
        boolean active
) {}
