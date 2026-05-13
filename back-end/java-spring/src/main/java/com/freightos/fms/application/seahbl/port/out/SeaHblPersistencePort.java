package com.freightos.fms.application.seahbl.port.out;

import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;

public interface SeaHblPersistencePort {
    void update(Long id, UpdateHouseBlCommand command);
}
