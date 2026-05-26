package com.freightos.admin.application.code.port;

import com.freightos.admin.application.code.port.command.CreatePortCommand;
import com.freightos.admin.domain.code.port.entity.Port;
import org.springframework.stereotype.Component;

@Component
public class PortFactory {

    public Port from(CreatePortCommand command) {
        return Port.create(command.portCode(), command.name(), command.nameEn(), command.countryCode(), command.portType(), command.active());
    }
}
