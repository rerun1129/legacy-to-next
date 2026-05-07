package com.freightos.fms.application.seahouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.seahouse.command.SearchSeaHouseCommand;
import com.freightos.fms.domain.seahouse.SeaHouseFilter;
import com.freightos.fms.application.seahouse.port.in.SeaHouseSearchUseCase;
import com.freightos.fms.application.seahouse.port.out.SeaHouseSearchPort;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import com.freightos.fms.domain.seahouse.PartnerKind;
import com.freightos.common.util.Nullables;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeaHouseSearchService implements SeaHouseSearchUseCase {

    private final SeaHouseSearchPort seaHouseSearchPort;

    @Override
    public PagedResult<SeaHouseSummary> searchSeaHouses(SearchSeaHouseCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        ShipmentType shipmentType = Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf);
        SalesClass salesClass = Nullables.mapOrNull(cmd.salesClass(), SalesClass::valueOf);
        Incoterms incoterms = Nullables.mapOrNull(cmd.incoterms(), Incoterms::valueOf);
        PartnerKind partnerKind = Nullables.mapIfHasText(cmd.partnerKind(), PartnerKind::valueOf);
        LoadType loadType = Nullables.mapIfHasText(cmd.loadType(), LoadType::valueOf);

        SeaHouseFilter filter = SeaHouseFilter.of(
                Bound.valueOf(cmd.bound()),
                cmd.dateFrom(), cmd.dateTo(),
                cmd.masterBlKind(), cmd.masterBlValue(),
                cmd.hblNo(),
                cmd.partyCode(),
                cmd.actualCustomerCode(),
                cmd.partnerCode(),
                cmd.linerCode(),
                cmd.portCode(),
                shipmentType,
                cmd.teamCode(), cmd.operatorCode(),
                cmd.salesManCode(),
                incoterms,
                cmd.vesselName(),
                cmd.voyageNo(),
                loadType
        ).withKinds(dateKind, partyKind, portKind, salesClass, partnerKind);

        return seaHouseSearchPort.searchSeaHouseSummaries(filter, pageRequest);
    }
}
