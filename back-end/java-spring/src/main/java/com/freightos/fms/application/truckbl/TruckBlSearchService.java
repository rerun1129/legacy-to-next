package com.freightos.fms.application.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.application.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.application.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TruckBlSearchService implements TruckBlSearchUseCase {

    private final TruckBlSearchPort truckBlSearchPort;

    @Override
    public PagedResult<TruckBlSummary> searchTruckBls(SearchTruckBlCommand cmd, PageRequest pageRequest) {
        TruckBlFilter filter = TruckBlFilter.of(
                cmd.bound() != null ? Bound.valueOf(cmd.bound()) : null,
                cmd.truckBlNo(),
                cmd.etdFrom(), cmd.etdTo(),
                cmd.truckerCode(), cmd.docPartnerCode(),
                cmd.partyCode(), cmd.portCode(),
                cmd.operatorCode(), cmd.teamCode()
        ).withKinds(
                cmd.dateKind() != null ? DateKind.valueOf(cmd.dateKind()) : null,
                cmd.partyKind() != null ? PartyKind.valueOf(cmd.partyKind()) : null,
                cmd.portKind() != null ? PortKind.valueOf(cmd.portKind()) : null
        );
        return truckBlSearchPort.searchTruckBlSummaries(filter, pageRequest);
    }
}
