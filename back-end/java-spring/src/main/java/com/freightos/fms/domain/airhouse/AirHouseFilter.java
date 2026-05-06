package com.freightos.fms.domain.airhouse;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.housebl.enums.SalesClass;

/**
 * Air House B/L 검색 필터.
 * endpoint가 도메인(AIR)을 식별하므로 jobDiv 필드를 포함하지 않는다.
 * bound는 라우트에서 항상 주입되므로 null 불가 필수값이다.
 */
public record AirHouseFilter(
    Bound bound,
    DateKind dateKind,
    String dateFrom,
    String dateTo,
    String masterAwbKind,
    String masterAwbValue,
    String hblNo,
    PartyKind partyKind,
    String partyCode,
    String actualCustomerCode,
    String settlePartnerCode,
    String airlineCode,
    PortKind portKind,
    String portCode,
    ShipmentType shipmentType,
    String teamCode,
    String operatorCode,
    SalesClass salesClass,
    String salesManCode,
    Incoterms incoterms
) {
    public static AirHouseFilter of(
            Bound bound,
            String dateFrom,
            String dateTo,
            String masterAwbKind,
            String masterAwbValue,
            String hblNo,
            String partyCode,
            String actualCustomerCode,
            String settlePartnerCode,
            String airlineCode,
            String portCode,
            ShipmentType shipmentType,
            String teamCode,
            String operatorCode,
            String salesManCode,
            Incoterms incoterms) {
        return new AirHouseFilter(
                bound, null, dateFrom, dateTo,
                masterAwbKind, masterAwbValue, hblNo,
                null, partyCode,
                actualCustomerCode, settlePartnerCode, airlineCode,
                null, portCode,
                shipmentType, teamCode, operatorCode,
                null, salesManCode, incoterms);
    }

    public AirHouseFilter withKinds(DateKind dateKind, PartyKind partyKind, PortKind portKind, SalesClass salesClass) {
        return new AirHouseFilter(
                this.bound(), dateKind,
                this.dateFrom(), this.dateTo(),
                this.masterAwbKind(), this.masterAwbValue(), this.hblNo(),
                partyKind, this.partyCode(),
                this.actualCustomerCode(), this.settlePartnerCode(), this.airlineCode(),
                portKind, this.portCode(),
                this.shipmentType(), this.teamCode(), this.operatorCode(),
                salesClass, this.salesManCode(), this.incoterms());
    }
}
