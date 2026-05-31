package com.freightos.fms.domain.seamaster;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;

/**
 * Sea Master B/L 검색 필터.
 * endpoint가 도메인(SEA)을 식별하므로 jobDiv 필드를 포함하지 않는다.
 * bound는 라우트에서 항상 주입되므로 null 불가 필수값이다.
 */
public record SeaMasterFilter(
    Bound bound,
    DateKind dateKind,
    String dateFrom,
    String dateTo,
    String masterBlKind,
    String masterBlValue,
    PartyKind partyKind,
    String partyCode,
    String linerCode,
    PortKind portKind,
    String portCode,
    String vesselName,
    String voyageNo,
    ShipmentType shipmentType,
    LoadType loadType,
    String teamCode
) {
    public static SeaMasterFilter of(
            Bound bound,
            String dateFrom,
            String dateTo,
            String masterBlKind,
            String masterBlValue,
            String partyCode,
            String linerCode,
            String portCode,
            String vesselName,
            String voyageNo,
            ShipmentType shipmentType,
            LoadType loadType,
            String teamCode) {
        return new SeaMasterFilter(
                bound, null, dateFrom, dateTo,
                masterBlKind, masterBlValue,
                null, partyCode,
                linerCode,
                null, portCode,
                vesselName, voyageNo,
                shipmentType, loadType,
                teamCode);
    }

    public SeaMasterFilter withKinds(DateKind dateKind, PartyKind partyKind, PortKind portKind) {
        return new SeaMasterFilter(
                this.bound(), dateKind,
                this.dateFrom(), this.dateTo(),
                this.masterBlKind(), this.masterBlValue(),
                partyKind, this.partyCode(),
                this.linerCode(),
                portKind, this.portCode(),
                this.vesselName(), this.voyageNo(),
                this.shipmentType(), this.loadType(),
                this.teamCode());
    }
}
