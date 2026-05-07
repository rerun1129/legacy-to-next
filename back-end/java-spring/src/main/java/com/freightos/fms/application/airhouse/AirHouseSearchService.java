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
import com.freightos.common.util.Nullables;
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
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        ShipmentType shipmentType = Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf);
        SalesClass salesClass = Nullables.mapOrNull(cmd.salesClass(), SalesClass::valueOf);
        Incoterms incoterms = Nullables.mapOrNull(cmd.incoterms(), Incoterms::valueOf);

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
