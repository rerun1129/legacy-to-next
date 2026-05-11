package com.freightos.fms.application.truckbl.port.out;

import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;

public interface TruckBlPersistencePort {
    void update(Long id, UpdateHouseBlCommand command);
}
