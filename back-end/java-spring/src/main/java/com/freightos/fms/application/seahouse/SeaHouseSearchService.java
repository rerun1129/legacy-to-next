package com.freightos.fms.application.seahouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.seahouse.command.SearchSeaHouseCommand;
import com.freightos.fms.domain.seahouse.SeaHouseFilter;
import com.freightos.fms.application.seahouse.port.in.SeaHouseSearchUseCase;
import com.freightos.fms.application.seahouse.port.out.SeaHouseSearchPort;
import com.freightos.fms.application.seahouse.projection.SeaHouseListItem;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import com.freightos.fms.domain.seahouse.PartnerKind;
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
public class SeaHouseSearchService implements SeaHouseSearchUseCase {

    private final SeaHouseSearchPort seaHouseSearchPort;
    private final CodeNameResolver codeNameResolver;

    @Override
    public PagedResult<SeaHouseListItem> searchSeaHouses(SearchSeaHouseCommand cmd, PageRequest pageRequest) {
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        ShipmentType shipmentType = Nullables.mapOrNull(cmd.shipmentType(), ShipmentType::valueOf);
        SalesClass salesClass = Nullables.mapOrNull(cmd.salesClass(), SalesClass::valueOf);
        Incoterms incoterms = Nullables.mapOrNull(cmd.incoterms(), Incoterms::valueOf);
        PartnerKind partnerKind = Nullables.mapIfHasText(cmd.partnerKind(), PartnerKind::valueOf);
        LoadType loadType = Nullables.mapIfHasText(cmd.loadType(), LoadType::valueOf);

        SeaHouseFilter filter = SeaHouseFilter.of(
                Bound.valueOf(cmd.bound()),
                cmd.dateFrom(), cmd.dateTo(),
                cmd.masterBlKind(), cmd.masterBlValue(),
                cmd.hblNo(),
                cmd.partyCode(),
                cmd.actualCustomerCode(),
                cmd.partnerCode(),
                cmd.linerCode(),
                cmd.portCode(),
                shipmentType,
                cmd.teamCode(), cmd.operatorCode(),
                cmd.salesManCode(),
                incoterms,
                cmd.vesselName(),
                cmd.voyageNo(),
                loadType
        ).withKinds(dateKind, partyKind, portKind, salesClass, partnerKind);

        PagedResult<SeaHouseSummary> summaries = seaHouseSearchPort.searchSeaHouseSummaries(filter, pageRequest);

        Map<String, String> customerNames = resolveCustomerNames(summaries.getContent());
        Map<String, String> carrierNames = resolveCarrierNames(summaries.getContent());

        return summaries.map(s -> toListItem(s, customerNames, carrierNames));
    }

    /** 페이지 전체에서 고객 관련 코드 6종을 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveCustomerNames(List<SeaHouseSummary> summaries) {
        Set<String> codes = new HashSet<>();
        for (SeaHouseSummary s : summaries) {
            addIfHasText(codes, s.shipperCode());
            addIfHasText(codes, s.consigneeCode());
            addIfHasText(codes, s.notifyCode());
            addIfHasText(codes, s.settlePartnerCode());
            addIfHasText(codes, s.docPartnerCode());
            addIfHasText(codes, s.actualCustomerCode());
        }
        return codeNameResolver.findCustomerNames(codes);
    }

    /** 페이지 전체에서 liner 코드를 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveCarrierNames(List<SeaHouseSummary> summaries) {
        Set<String> codes = new HashSet<>();
        for (SeaHouseSummary s : summaries) {
            addIfHasText(codes, s.linerCode());
        }
        return codeNameResolver.findCarrierNames(codes);
    }

    private static void addIfHasText(Set<String> target, String code) {
        if (code != null && !code.isBlank()) {
            target.add(code);
        }
    }

    private static SeaHouseListItem toListItem(
            SeaHouseSummary s,
            Map<String, String> customerNames,
            Map<String, String> carrierNames
    ) {
        return new SeaHouseListItem(
                s.id(),
                s.hblNo(),
                s.bound(),
                s.mblNo(),
                s.shipmentType(),
                s.etd(),
                s.eta(),
                s.grossWeightKg(),
                s.rton(),
                s.pkgQty(),
                s.pkgUnit(),
                s.polCode(),
                s.podCode(),
                s.shipperCode(),
                nameOrEmpty(customerNames, s.shipperCode()),
                s.consigneeCode(),
                nameOrEmpty(customerNames, s.consigneeCode()),
                s.notifyCode(),
                nameOrEmpty(customerNames, s.notifyCode()),
                s.settlePartnerCode(),
                nameOrEmpty(customerNames, s.settlePartnerCode()),
                s.docPartnerCode(),
                nameOrEmpty(customerNames, s.docPartnerCode()),
                s.linerCode(),
                nameOrEmpty(carrierNames, s.linerCode()),
                s.masterRefNo(),
                s.freightTerm(),
                s.incoterms(),
                s.actualCustomerCode(),
                nameOrEmpty(customerNames, s.actualCustomerCode()),
                s.salesManCode(),
                s.teamCode(),
                s.loadType(),
                s.cbm(),
                s.deliveryCode(),
                s.vesselName(),
                s.voyageNo(),
                s.cntr20Qty(),
                s.cntr40Qty(),
                s.lengthFeetSum()
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
