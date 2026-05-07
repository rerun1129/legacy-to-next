package com.freightos.fms.application.airmaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.airmaster.command.SearchAirMasterCommand;
import com.freightos.fms.domain.airmaster.AirMasterFilter;
import com.freightos.fms.application.airmaster.port.in.AirMasterSearchUseCase;
import com.freightos.fms.application.airmaster.port.out.AirMasterSearchPort;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.common.util.Nullables;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirMasterSearchService implements AirMasterSearchUseCase {

    private final AirMasterSearchPort airMasterSearchPort;

    @Override
    public PagedResult<AirMasterSummary> searchAirMasters(SearchAirMasterCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        ShipmentType shipmentType = Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf);

        AirMasterFilter filter = AirMasterFilter.of(
                Bound.valueOf(cmd.bound()),
                cmd.dateFrom(), cmd.dateTo(),
                cmd.masterAwbKind(), cmd.masterAwbValue(),
                cmd.partyCode(),
                cmd.airlineCode(),
                cmd.portCode(),
                shipmentType,
                cmd.teamCode()
        ).withKinds(dateKind, partyKind, portKind);

        return airMasterSearchPort.searchAirMasterSummaries(filter, pageRequest);
    }
}
