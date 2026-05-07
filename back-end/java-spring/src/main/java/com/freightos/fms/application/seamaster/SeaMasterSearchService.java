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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeaMasterSearchService implements SeaMasterSearchUseCase {

    private final SeaMasterSearchPort seaMasterSearchPort;

    @Override
    public PagedResult<SeaMasterSummary> searchSeaMasters(SearchSeaMasterCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = cmd.dateKind() != null ? DateKind.valueOf(cmd.dateKind()) : null;
        PartyKind partyKind = cmd.partyKind() != null ? PartyKind.valueOf(cmd.partyKind()) : null;
        PortKind portKind = cmd.portKind() != null ? PortKind.valueOf(cmd.portKind()) : null;
        ShipmentType shipmentType = cmd.shipmentType() != null ? ShipmentType.valueOf(cmd.shipmentType()) : null;
        LoadType loadType = StringUtils.hasText(cmd.loadType()) ? LoadType.valueOf(cmd.loadType()) : null;

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
