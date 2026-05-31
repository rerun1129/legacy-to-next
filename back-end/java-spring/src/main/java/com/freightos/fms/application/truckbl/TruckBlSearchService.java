package com.freightos.fms.application.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.truckbl.PartnerKind;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.application.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.application.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.application.truckbl.projection.TruckBlListItem;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;
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
public class TruckBlSearchService implements TruckBlSearchUseCase {

    private final TruckBlSearchPort truckBlSearchPort;
    private final CodeNameResolver codeNameResolver;

    @Override
    public PagedResult<TruckBlListItem> searchTruckBls(SearchTruckBlCommand cmd, PageRequest pageRequest) {
        PartnerKind partnerKind = Nullables.mapIfHasText(cmd.partnerKind(), PartnerKind::valueOf);

        TruckBlFilter filter = TruckBlFilter.of(
                Nullables.mapOrNull(cmd.bound(), Bound::valueOf),
                cmd.truckBlNo(),
                cmd.etdFrom(), cmd.etdTo(),
                cmd.truckerCode(),
                cmd.partyCode(),
                cmd.partnerCode(),
                cmd.portCode(),
                cmd.operatorCode(), cmd.teamCode()
        ).withKinds(
                Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf),
                Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf),
                Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf),
                partnerKind
        );

        PagedResult<TruckBlSummary> summaries = truckBlSearchPort.searchTruckBlSummaries(filter, pageRequest);

        Map<String, String> teamNames = resolveTeamNames(summaries.getContent());

        return summaries.map(s -> toListItem(s, teamNames));
    }

    /** 페이지 전체에서 team 코드를 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveTeamNames(List<TruckBlSummary> summaries) {
        Set<String> codes = new HashSet<>();
        for (TruckBlSummary s : summaries) {
            addIfHasText(codes, s.teamCode());
        }
        return codeNameResolver.findTeamNames(codes);
    }

    private static void addIfHasText(Set<String> target, String code) {
        if (code != null && !code.isBlank()) {
            target.add(code);
        }
    }

    private static TruckBlListItem toListItem(TruckBlSummary s, Map<String, String> teamNames) {
        return new TruckBlListItem(
                s.id(),
                s.hblNo(),
                s.jobDiv(),
                s.bound(),
                s.polCode(),
                s.podCode(),
                s.etd(),
                s.eta(),
                s.shipperCode(),
                s.consigneeCode(),
                s.notifyCode(),
                s.docPartnerCode(),
                s.truckerCode(),
                s.pkgQty(),
                s.pkgUnit(),
                s.grossWeightKg(),
                s.cbm(),
                s.createdAt(),
                s.teamCode(),
                nameOrEmpty(teamNames, s.teamCode())
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
