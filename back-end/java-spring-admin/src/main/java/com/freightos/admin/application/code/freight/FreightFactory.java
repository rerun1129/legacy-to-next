package com.freightos.admin.application.code.freight;

import com.freightos.admin.application.code.freight.command.CreateFreightCommand;
import com.freightos.admin.domain.code.freight.entity.Freight;
import org.springframework.stereotype.Component;

@Component
public class FreightFactory {

    public Freight from(CreateFreightCommand command) {
        return Freight.create(command.freightCode(), command.name(), command.nameEn(), command.description(), command.freightUnit(), command.freightGroup(), command.active());
    }
}
