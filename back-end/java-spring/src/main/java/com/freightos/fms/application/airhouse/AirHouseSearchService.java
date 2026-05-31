package com.freightos.fms.application.airhouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.airhouse.command.SearchAirHouseCommand;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.domain.airhouse.AirHouseFilter;
import com.freightos.fms.application.airhouse.port.in.AirHouseSearchUseCase;
import com.freightos.fms.application.airhouse.port.out.AirHouseSearchPort;
import com.freightos.fms.application.airhouse.projection.AirHouseListItem;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.housebl.enums.SalesClass;
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
public class AirHouseSearchService implements AirHouseSearchUseCase {

    private final AirHouseSearchPort airHouseSearchPort;
    private final CodeNameResolver codeNameResolver;

    @Override
    public PagedResult<AirHouseListItem> searchAirHouses(SearchAirHouseCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        ShipmentType shipmentType = Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf);
        SalesClass salesClass = Nullables.mapOrNull(cmd.salesClass(), SalesClass::valueOf);
        Incoterms incoterms = Nullables.mapOrNull(cmd.incoterms(), Incoterms::valueOf);

        AirHouseFilter filter = AirHouseFilter.of(
                Bound.valueOf(cmd.bound()),
                cmd.dateFrom(), cmd.dateTo(),
                cmd.masterAwbKind(), cmd.masterAwbValue(),
                cmd.hblNo(),
                cmd.partyCode(),
                cmd.actualCustomerCode(), cmd.settlePartnerCode(),
                cmd.airlineCode(),
                cmd.portCode(),
                shipmentType,
                cmd.teamCode(), cmd.operatorCode(),
                cmd.salesManCode(),
                incoterms
        ).withKinds(dateKind, partyKind, portKind, salesClass);

        PagedResult<AirHouseSummary> summaries = airHouseSearchPort.searchAirHouseSummaries(filter, pageRequest);

        Map<String, String> teamNames = resolveTeamNames(summaries.getContent());

        return summaries.map(s -> toListItem(s, teamNames));
    }

    /** 페이지 전체에서 team 코드를 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveTeamNames(List<AirHouseSummary> summaries) {
        Set<String> codes = new HashSet<>();
        for (AirHouseSummary s : summaries) {
            addIfHasText(codes, s.teamCode());
        }
        return codeNameResolver.findTeamNames(codes);
    }

    private static void addIfHasText(Set<String> target, String code) {
        if (code != null && !code.isBlank()) {
            target.add(code);
        }
    }

    private static AirHouseListItem toListItem(AirHouseSummary s, Map<String, String> teamNames) {
        return new AirHouseListItem(
                s.id(),
                s.hblNo(),
                s.bound(),
                s.mblNo(),
                s.shipmentType(),
                s.etd(),
                s.eta(),
                s.grossWeightKg(),
                s.chargeWeightKg(),
                s.pkgQty(),
                s.pkgUnit(),
                s.polCode(),
                s.podCode(),
                s.shipperCode(),
                s.consigneeCode(),
                s.notifyCode(),
                s.settlePartnerCode(),
                s.docPartnerCode(),
                s.airlineCode(),
                s.masterRefNo(),
                s.freightTerm(),
                s.incoterms(),
                s.actualCustomerCode(),
                s.salesManCode(),
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
