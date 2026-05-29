package com.freightos.fms.application.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.truckbl.PartnerKind;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.application.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.application.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;
import com.freightos.common.util.Nullables;
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
        PartnerKind partnerKind = Nullables.mapIfHasText(cmd.partnerKind(), PartnerKind::valueOf);

        TruckBlFilter filter = TruckBlFilter.of(
                Nullables.mapOrNull(cmd.bound(), Bound::valueOf),
                cmd.truckBlNo(),
                cmd.etdFrom(), cmd.etdTo(),
                cmd.truckerCode(),
                cmd.partyCode(),
                cmd.partnerCode(),
                cmd.portCode(),
                cmd.operatorCode(), cmd.teamCode()
        ).withKinds(
                Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf),
                Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf),
                Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf),
                partnerKind
        );

        return truckBlSearchPort.searchTruckBlSummaries(filter, pageRequest);
    }
}
