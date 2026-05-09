package com.freightos.fms.application.truckbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.truckbl.port.in.TruckBlUseCase;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TruckBlService implements TruckBlUseCase {

    private final HouseBlPort houseBlPort;
    private final TruckBlFactory truckBlFactory;

    @Override
    public TruckBlDetailResult findTruckBlById(Long id) {
        HouseBl entity = houseBlPort.findHouseBlById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));
        if (!(entity instanceof HouseBlTruck truck))
            throw new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND);
        return truckBlFactory.toDetailResult(truck);
    }
}
