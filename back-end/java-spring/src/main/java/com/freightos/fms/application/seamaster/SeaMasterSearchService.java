package com.freightos.fms.application.seamaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.seamaster.command.SearchSeaMasterCommand;
import com.freightos.fms.domain.seamaster.SeaMasterFilter;
import com.freightos.fms.application.seamaster.port.in.SeaMasterSearchUseCase;
import com.freightos.fms.application.seamaster.port.out.SeaMasterSearchPort;
import com.freightos.fms.application.seamaster.projection.SeaMasterListItem;
import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.common.util.Nullables;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeaMasterSearchService implements SeaMasterSearchUseCase {

    private final SeaMasterSearchPort seaMasterSearchPort;
    private final CodeNameResolver codeNameResolver;

    @Override
    public PagedResult<SeaMasterListItem> searchSeaMasters(SearchSeaMasterCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        ShipmentType shipmentType = Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf);
        LoadType loadType = Nullables.mapIfHasText(cmd.loadType(), LoadType::valueOf);

        SeaMasterFilter filter = SeaMasterFilter.of(
                Bound.valueOf(cmd.bound()),
                cmd.dateFrom(), cmd.dateTo(),
                cmd.masterBlKind(), cmd.masterBlValue(),
                cmd.partyCode(),
                cmd.linerCode(),
                cmd.portCode(),
                cmd.vesselName(),
                cmd.voyageNo(),
                shipmentType,
                loadType,
                cmd.teamCode()
        ).withKinds(dateKind, partyKind, portKind);

        PagedResult<SeaMasterSummary> summaries = seaMasterSearchPort.searchSeaMasterSummaries(filter, pageRequest);

        Map<String, String> teamNames = resolveTeamNames(summaries.getContent());

        return summaries.map(s -> toListItem(s, teamNames));
    }

    /** 페이지 전체에서 team 코드를 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveTeamNames(List<SeaMasterSummary> summaries) {
        Set<String> codes = new HashSet<>();
        for (SeaMasterSummary s : summaries) {
            addIfHasText(codes, s.teamCode());
        }
        return codeNameResolver.findTeamNames(codes);
    }

    private static void addIfHasText(Set<String> target, String code) {
        if (code != null && !code.isBlank()) {
            target.add(code);
        }
    }

    private static SeaMasterListItem toListItem(SeaMasterSummary s, Map<String, String> teamNames) {
        return new SeaMasterListItem(
                s.id(),
                s.bound(),
                s.mblNo(),
                s.shipmentType(),
                s.etd(),
                s.eta(),
                s.grossWeightKg(),
                s.rton(),
                s.pkgQty(),
                s.pkgUnit(),
                s.houseBlCount(),
                s.polCode(),
                s.podCode(),
                s.shipperCode(),
                s.consigneeCode(),
                s.notifyCode(),
                s.settlePartnerCode(),
                s.linerCode(),
                s.masterRefNo(),
                s.freightTerm(),
                s.operatorCode(),
                s.teamCode(),
                nameOrEmpty(teamNames, s.teamCode()),
                s.vesselName(),
                s.voyageNo(),
                s.loadType(),
                s.cbm()
        );
    }

    /** 코드가 null이거나 맵에 없으면 빈 문자열 반환. */
    private static String nameOrEmpty(Map<String, String> nameMap, String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return nameMap.getOrDefault(code, "");
    }
}
