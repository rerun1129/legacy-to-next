package com.freightos.admin.application.code.carrier.command;

import com.freightos.admin.domain.code.carrier.entity.CarrierType;

public record UpdateCarrierCommand(
        String name,
        String nameEn,
        CarrierType carrierType,
        String carrierAddress,
        String ediCode,
        boolean active
) {}
