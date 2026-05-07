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
        DateKind dateKind = cmd.dateKind() != null ? DateKind.valueOf(cmd.dateKind()) : null;
        PartyKind partyKind = cmd.partyKind() != null ? PartyKind.valueOf(cmd.partyKind()) : null;
        PortKind portKind = cmd.portKind() != null ? PortKind.valueOf(cmd.portKind()) : null;
        ShipmentType shipmentType = cmd.shipmentType() != null ? ShipmentType.valueOf(cmd.shipmentType()) : null;
        SalesClass salesClass = cmd.salesClass() != null ? SalesClass.valueOf(cmd.salesClass()) : null;
        Incoterms incoterms = cmd.incoterms() != null ? Incoterms.valueOf(cmd.incoterms()) : null;
        PartnerKind partnerKind = (cmd.partnerKind() != null && !cmd.partnerKind().isBlank()) ? PartnerKind.valueOf(cmd.partnerKind()) : null;
        LoadType loadType = (cmd.loadType() != null && !cmd.loadType().isBlank()) ? LoadType.valueOf(cmd.loadType()) : null;

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
