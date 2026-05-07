package com.freightos.fms.domain.seahouse;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.housebl.enums.SalesClass;

/**
 * Sea House B/L 검색 필터.
 * endpoint가 도메인(SEA)을 식별하므로 jobDiv 필드를 포함하지 않는다.
 * bound는 라우트에서 항상 주입되므로 null 불가 필수값이다.
 */
public record SeaHouseFilter(
    Bound bound,
    DateKind dateKind,
    String dateFrom,
    String dateTo,
    String masterBlKind,
    String masterBlValue,
    String hblNo,
    PartyKind partyKind,
    String partyCode,
    String actualCustomerCode,
    PartnerKind partnerKind,
    String partnerCode,
    String linerCode,
    PortKind portKind,
    String portCode,
    String vesselName,
    String voyageNo,
    ShipmentType shipmentType,
    String teamCode,
    String operatorCode,
    SalesClass salesClass,
    String salesManCode,
    Incoterms incoterms,
    LoadType loadType
) {
    public static SeaHouseFilter of(
            Bound bound,
            String dateFrom,
            String dateTo,
            String masterBlKind,
            String masterBlValue,
            String hblNo,
            String partyCode,
            String actualCustomerCode,
            String partnerCode,
            String linerCode,
            String portCode,
            ShipmentType shipmentType,
            String teamCode,
            String operatorCode,
            String salesManCode,
            Incoterms incoterms,
            String vesselName,
            String voyageNo,
            LoadType loadType) {
        return new SeaHouseFilter(
                bound, null, dateFrom, dateTo,
                masterBlKind, masterBlValue, hblNo,
                null, partyCode,
                actualCustomerCode,
                null, partnerCode,
                linerCode,
                null, portCode,
                vesselName, voyageNo,
                shipmentType, teamCode, operatorCode,
                null, salesManCode, incoterms, loadType);
    }

    public SeaHouseFilter withKinds(
            DateKind dateKind,
            PartyKind partyKind,
            PortKind portKind,
            SalesClass salesClass,
            PartnerKind partnerKind) {
        return new SeaHouseFilter(
                this.bound(), dateKind,
                this.dateFrom(), this.dateTo(),
                this.masterBlKind(), this.masterBlValue(), this.hblNo(),
                partyKind, this.partyCode(),
                this.actualCustomerCode(),
                partnerKind, this.partnerCode(),
                this.linerCode(),
                portKind, this.portCode(),
                this.vesselName(), this.voyageNo(),
                this.shipmentType(), this.teamCode(), this.operatorCode(),
                salesClass, this.salesManCode(), this.incoterms(), this.loadType());
    }
}
