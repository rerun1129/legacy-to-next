package com.freightos.fms.application.nonbl.port.out;

import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;

public interface NonBlPersistencePort {
    void update(Long id, UpdateHouseBlCommand command);
}
