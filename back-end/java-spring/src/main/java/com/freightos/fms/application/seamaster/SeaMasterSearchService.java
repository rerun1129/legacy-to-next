package com.freightos.fms.application.seamaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.seamaster.command.SearchSeaMasterCommand;
import com.freightos.fms.domain.seamaster.SeaMasterFilter;
import com.freightos.fms.application.seamaster.port.in.SeaMasterSearchUseCase;
import com.freightos.fms.application.seamaster.port.out.SeaMasterSearchPort;
import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.LoadType;
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
public class SeaMasterSearchService implements SeaMasterSearchUseCase {

    private final SeaMasterSearchPort seaMasterSearchPort;

    @Override
    public PagedResult<SeaMasterSummary> searchSeaMasters(SearchSeaMasterCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        ShipmentType shipmentType = Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf);
        LoadType loadType = Nullables.mapIfHasText(cmd.loadType(), LoadType::valueOf);

        SeaMasterFilter filter = SeaMasterFilter.of(
                Bound.valueOf(cmd.bound()),
                cmd.dateFrom(), cmd.dateTo(),
                cmd.masterBlKind(), cmd.masterBlValue(),
                cmd.partyCode(),
                cmd.linerCode(),
                cmd.portCode(),
                cmd.vesselName(),
                cmd.voyageNo(),
                shipmentType,
                loadType
        ).withKinds(dateKind, partyKind, portKind);

        return seaMasterSearchPort.searchSeaMasterSummaries(filter, pageRequest);
    }
}
