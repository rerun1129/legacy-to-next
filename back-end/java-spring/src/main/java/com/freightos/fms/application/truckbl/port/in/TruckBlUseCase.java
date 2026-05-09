package com.freightos.fms.application.truckbl.port.in;

import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;

public interface TruckBlUseCase {
    TruckBlDetailResult findTruckBlById(Long id);
}
