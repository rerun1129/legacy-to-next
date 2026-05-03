package com.freightos.fms.domain.housebl;

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
    String etdTo
) {
    public static HouseBlFilter of(
            com.freightos.fms.domain.housebl.enums.JobDiv jobDiv,
            com.freightos.fms.domain.common.enums.Bound bound,
            String hblNo, String mblNo,
            String shipperCode, String consigneeCode,
            String polCode, String podCode,
            String etdFrom, String etdTo) {
        return new HouseBlFilter(jobDiv, bound, hblNo, mblNo, shipperCode, consigneeCode, polCode, podCode, etdFrom, etdTo);
    }
}
