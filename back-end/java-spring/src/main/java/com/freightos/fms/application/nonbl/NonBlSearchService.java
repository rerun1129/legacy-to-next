package com.freightos.fms.application.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.application.nonbl.port.in.NonBlSearchUseCase;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonBlSearchService implements NonBlSearchUseCase {

    private final NonBlSearchPort nonBlSearchPort;

    @Override
    public PagedResult<NonBlSummary> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest) {
        NonBlFilter filter = NonBlFilter.of(
                cmd.bound() != null ? Bound.valueOf(cmd.bound()) : null,
                cmd.hblNo(),
                cmd.etdFrom(), cmd.etdTo(),
                cmd.linerCode(),
                cmd.partyCode(), cmd.portCode(),
                cmd.vessel(), cmd.voyage(),
                cmd.operatorCode(), cmd.teamCode()
        ).withKinds(
                cmd.dateKind() != null ? DateKind.valueOf(cmd.dateKind()) : null,
                cmd.partyKind() != null ? PartyKind.valueOf(cmd.partyKind()) : null,
                cmd.portKind() != null ? PortKind.valueOf(cmd.portKind()) : null
        );
        return nonBlSearchPort.searchNonBlSummaries(filter, pageRequest);
    }
}
