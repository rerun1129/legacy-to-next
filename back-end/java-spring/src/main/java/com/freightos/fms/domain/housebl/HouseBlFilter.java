package com.freightos.fms.domain.housebl;

import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;

public record HouseBlFilter(
    com.freightos.fms.domain.housebl.enums.JobDiv jobDiv,
    com.freightos.fms.domain.common.enums.Bound bound,
    String hblNo,
    String mblNo,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etdFrom,
    String etdTo,
    String vessel,
    String voyage,
    String linerCode,
    String operatorCode,
    String teamCode,
    String partyCode,
    String portCode,
    DateKind dateKind,
    PartyKind partyKind,
    PortKind portKind
) {
    // of() 파라미터 시그니처는 기존 그대로 유지 — 테스트 6곳 보호
    public static HouseBlFilter of(
            com.freightos.fms.domain.housebl.enums.JobDiv jobDiv,
            com.freightos.fms.domain.common.enums.Bound bound,
            String hblNo, String mblNo,
            String shipperCode, String consigneeCode,
            String polCode, String podCode,
            String etdFrom, String etdTo,
            String vessel, String voyage,
            String linerCode, String operatorCode,
            String teamCode, String partyCode,
            String portCode) {
        return new HouseBlFilter(jobDiv, bound, hblNo, mblNo, shipperCode, consigneeCode,
                polCode, podCode, etdFrom, etdTo,
                vessel, voyage, linerCode, operatorCode, teamCode, partyCode, portCode,
                null, null, null);
    }

    public HouseBlFilter withKinds(DateKind dateKind, PartyKind partyKind, PortKind portKind) {
        return new HouseBlFilter(
                this.jobDiv(), this.bound(),
                this.hblNo(), this.mblNo(),
                this.shipperCode(), this.consigneeCode(),
                this.polCode(), this.podCode(),
                this.etdFrom(), this.etdTo(),
                this.vessel(), this.voyage(),
                this.linerCode(), this.operatorCode(),
                this.teamCode(), this.partyCode(), this.portCode(),
                dateKind, partyKind, portKind);
    }
}
