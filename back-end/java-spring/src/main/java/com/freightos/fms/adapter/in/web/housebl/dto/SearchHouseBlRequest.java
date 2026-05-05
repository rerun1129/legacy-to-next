package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchHouseBlRequest(
    @NotNull JobDiv jobDiv,
    Bound bound,
    String hblNo,
    String mblNo,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etdFrom,
    String etdTo,
    String linerCode,
    String partyCode,
    String portCode,
    String vessel,
    String voyage,
    String operatorCode,
    String teamCode,
    @NotNull @Min(0) Integer page,
    @NotNull @Min(1) Integer size,
    DateKind dateKind,
    PartyKind partyKind,
    PortKind portKind
) {}
