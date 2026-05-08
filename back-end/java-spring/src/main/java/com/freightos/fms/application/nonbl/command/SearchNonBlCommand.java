package com.freightos.fms.application.nonbl.command;

import com.freightos.common.util.Nullables;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.nonbl.NonBlFilter;

public record SearchNonBlCommand(
    String bound,
    String hblNo,
    String etdFrom,
    String etdTo,
    String linerCode,
    String partyCode,
    String portCode,
    String vessel,
    String voyage,
    String operatorCode,
    String teamCode,
    String dateKind,
    String partyKind,
    String portKind
) {
    public NonBlFilter toFilter() {
        return NonBlFilter.of(
                Nullables.mapOrNull(bound, Bound::valueOf),
                hblNo, etdFrom, etdTo,
                linerCode, partyCode, portCode,
                vessel, voyage, operatorCode, teamCode
        ).withKinds(
                Nullables.mapOrNull(dateKind, DateKind::valueOf),
                Nullables.mapOrNull(partyKind, PartyKind::valueOf),
                Nullables.mapOrNull(portKind, PortKind::valueOf)
        );
    }
}
