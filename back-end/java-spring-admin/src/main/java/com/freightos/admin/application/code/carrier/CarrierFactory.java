package com.freightos.admin.application.code.carrier;

import com.freightos.admin.application.code.carrier.command.CreateCarrierCommand;
import com.freightos.admin.domain.code.carrier.entity.Carrier;
import org.springframework.stereotype.Component;

@Component
public class CarrierFactory {

    public Carrier from(CreateCarrierCommand command) {
        return Carrier.create(command.carrierCode(), command.name(), command.nameEn(), command.carrierType(), command.carrierAddress(), command.ediCode(), command.active());
    }
}
