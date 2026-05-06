package com.freightos.fms.domain.airmaster;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;

/**
 * Air Master B/L 검색 필터.
 * endpoint가 도메인(AIR)을 식별하므로 jobDiv 필드를 포함하지 않는다.
 * bound는 라우트에서 항상 주입되므로 null 불가 필수값이다.
 */
public record AirMasterFilter(
    Bound bound,
    DateKind dateKind,
    String dateFrom,
    String dateTo,
    String masterAwbKind,
    String masterAwbValue,
    PartyKind partyKind,
    String partyCode,
    String airlineCode,
    PortKind portKind,
    String portCode,
    ShipmentType shipmentType,
    String teamCode
) {
    public static AirMasterFilter of(
            Bound bound,
            String dateFrom,
            String dateTo,
            String masterAwbKind,
            String masterAwbValue,
            String partyCode,
            String airlineCode,
            String portCode,
            ShipmentType shipmentType,
            String teamCode) {
        return new AirMasterFilter(
                bound, null, dateFrom, dateTo,
                masterAwbKind, masterAwbValue,
                null, partyCode,
                airlineCode,
                null, portCode,
                shipmentType, teamCode);
    }

    public AirMasterFilter withKinds(DateKind dateKind, PartyKind partyKind, PortKind portKind) {
        return new AirMasterFilter(
                this.bound(), dateKind,
                this.dateFrom(), this.dateTo(),
                this.masterAwbKind(), this.masterAwbValue(),
                partyKind, this.partyCode(),
                this.airlineCode(),
                portKind, this.portCode(),
                this.shipmentType(), this.teamCode());
    }
}
