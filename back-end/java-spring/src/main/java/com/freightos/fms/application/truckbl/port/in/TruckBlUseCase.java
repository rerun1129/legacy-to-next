package com.freightos.fms.application.truckbl.port.in;

import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailView;

import java.util.List;

public interface TruckBlUseCase {
    TruckBlDetailView findTruckBlById(Long id);
    Long createTruckBl(CreateHouseBlCommand command);
    void updateTruckBl(Long id, UpdateHouseBlCommand command);
    void deleteTruckBlById(Long id);
    List<Long> findTruckBlKeysByHblNoExact(String hblNo);
    void changeTruckBlHblNo(Long id, ChangeHouseBlNoCommand command);
}
