package com.freightos.fms.application.airhouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.airhouse.command.SearchAirHouseCommand;
import com.freightos.fms.domain.airhouse.AirHouseFilter;
import com.freightos.fms.application.airhouse.port.in.AirHouseSearchUseCase;
import com.freightos.fms.application.airhouse.port.out.AirHouseSearchPort;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirHouseSearchService implements AirHouseSearchUseCase {

    private final AirHouseSearchPort airHouseSearchPort;

    @Override
    public PagedResult<AirHouseSummary> searchAirHouses(SearchAirHouseCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = cmd.dateKind() != null ? DateKind.valueOf(cmd.dateKind()) : null;
        PartyKind partyKind = cmd.partyKind() != null ? PartyKind.valueOf(cmd.partyKind()) : null;
        PortKind portKind = cmd.portKind() != null ? PortKind.valueOf(cmd.portKind()) : null;
        ShipmentType shipmentType = cmd.shipmentType() != null ? ShipmentType.valueOf(cmd.shipmentType()) : null;
        SalesClass salesClass = cmd.salesClass() != null ? SalesClass.valueOf(cmd.salesClass()) : null;
        Incoterms incoterms = cmd.incoterms() != null ? Incoterms.valueOf(cmd.incoterms()) : null;

        AirHouseFilter filter = AirHouseFilter.of(
                Bound.valueOf(cmd.bound()),
                cmd.dateFrom(), cmd.dateTo(),
                cmd.masterAwbKind(), cmd.masterAwbValue(),
                cmd.hblNo(),
                cmd.partyCode(),
                cmd.actualCustomerCode(), cmd.settlePartnerCode(),
                cmd.airlineCode(),
                cmd.portCode(),
                shipmentType,
                cmd.teamCode(), cmd.operatorCode(),
                cmd.salesManCode(),
                incoterms
        ).withKinds(dateKind, partyKind, portKind, salesClass);

        return airHouseSearchPort.searchAirHouseSummaries(filter, pageRequest);
    }
}
