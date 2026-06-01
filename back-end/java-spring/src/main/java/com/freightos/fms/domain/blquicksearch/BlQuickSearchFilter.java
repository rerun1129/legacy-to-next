package com.freightos.fms.domain.blquicksearch;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;

/**
 * House/Master BL 통합 자동완성 검색 조건.
 * POL/POD는 토글 없이 독립 2슬롯으로 관리한다.
 */
public record BlQuickSearchFilter(
    JobDiv jobDiv,
    Bound bound,
    DateKind dateKind,
    String dateFrom,
    String dateTo,
    String teamCode,
    String operatorCode,
    String salesManCode,
    PartyKind partyKind,
    String partyCode,
    String polCode,
    String podCode,
    String blNo
) {}
