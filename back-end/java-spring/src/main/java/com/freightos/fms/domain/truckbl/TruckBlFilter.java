package com.freightos.fms.domain.truckbl;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;

/**
 * Truck B/L 검색 필터.
 * endpoint가 도메인(TRUCK)을 식별하므로 jobDiv 필드를 포함하지 않는다.
 */
public record TruckBlFilter(
    Bound bound,
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
    DateKind dateKind,
    PartyKind partyKind,
    PortKind portKind
) {
    public static TruckBlFilter of(
            Bound bound,
            String hblNo,
            String etdFrom,
            String etdTo,
            String linerCode,
            String partyCode,
            String portCode,
            String vessel,
            String voyage,
            String operatorCode,
            String teamCode) {
        return new TruckBlFilter(bound, hblNo, etdFrom, etdTo, linerCode,
                partyCode, portCode, vessel, voyage, operatorCode, teamCode,
                null, null, null);
    }

    public TruckBlFilter withKinds(DateKind dateKind, PartyKind partyKind, PortKind portKind) {
        return new TruckBlFilter(
                this.bound(), this.hblNo(),
                this.etdFrom(), this.etdTo(),
                this.linerCode(), this.partyCode(), this.portCode(),
                this.vessel(), this.voyage(),
                this.operatorCode(), this.teamCode(),
                dateKind, partyKind, portKind);
    }
}
