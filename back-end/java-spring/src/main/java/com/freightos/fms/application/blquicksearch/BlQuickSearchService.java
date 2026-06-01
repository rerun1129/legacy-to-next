package com.freightos.fms.application.blquicksearch;

import com.freightos.fms.application.blquicksearch.command.BlQuickSearchCommand;
import com.freightos.fms.application.blquicksearch.port.in.BlQuickSearchUseCase;
import com.freightos.fms.application.blquicksearch.port.out.BlQuickSearchPort;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import com.freightos.fms.domain.blquicksearch.BlQuickSearchFilter;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.common.util.Nullables;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlQuickSearchService implements BlQuickSearchUseCase {

    private static final int MAX_LIMIT = 20;

    private final BlQuickSearchPort blQuickSearchPort;

    @Override
    public List<BlQuickSearchSummary> quickSearch(BlQuickSearchCommand cmd) {
        int limit = resolveLimit(cmd.limit());

        JobDiv jobDiv = Nullables.mapIfHasText(cmd.jobDiv(), JobDiv::valueOf);
        Bound bound = Nullables.mapIfHasText(cmd.bound(), Bound::valueOf);
        DateKind dateKind = Nullables.mapIfHasText(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapIfHasText(cmd.partyKind(), PartyKind::valueOf);

        BlQuickSearchFilter filter = new BlQuickSearchFilter(
                jobDiv, bound, dateKind,
                cmd.dateFrom(), cmd.dateTo(),
                cmd.teamCode(), cmd.operatorCode(), cmd.salesManCode(),
                partyKind, cmd.partyCode(),
                cmd.polCode(), cmd.podCode(),
                cmd.blNo()
        );

        List<BlQuickSearchSummary> houseResults = blQuickSearchPort.searchHouse(filter, limit);
        List<BlQuickSearchSummary> masterResults = resolveMasterResults(filter, limit, cmd.salesManCode(), jobDiv);

        List<BlQuickSearchSummary> merged = new ArrayList<>(houseResults.size() + masterResults.size());
        merged.addAll(houseResults);
        merged.addAll(masterResults);

        return merged.stream()
                .sorted(Comparator
                        .comparing(BlQuickSearchSummary::blNo, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(BlQuickSearchSummary::blType)
                        .thenComparing(BlQuickSearchSummary::id))
                .limit(limit)
                .toList();
    }

    /**
     * Master 검색 실행 조건: salesManCode 미설정 AND (jobDiv가 null·SEA·AIR).
     * House 전용 필드(salesMan)를 지정한 경우 또는 Master 미지원 jobDiv(TRUCK·NON_BL)이면 빈 리스트 반환.
     */
    private List<BlQuickSearchSummary> resolveMasterResults(
            BlQuickSearchFilter filter, int limit, String salesManCode, JobDiv jobDiv) {
        if (StringUtils.hasText(salesManCode)) {
            return List.of();
        }
        if (jobDiv == JobDiv.TRUCK || jobDiv == JobDiv.NON_BL) {
            return List.of();
        }
        return blQuickSearchPort.searchMaster(filter, limit);
    }

    private static int resolveLimit(Integer raw) {
        if (raw == null || raw <= 0) return MAX_LIMIT;
        return Math.min(raw, MAX_LIMIT);
    }
}
