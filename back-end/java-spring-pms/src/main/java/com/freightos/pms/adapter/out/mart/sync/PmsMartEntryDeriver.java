package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlLineEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.adapter.out.mart.document.PmsDocDtEntryDocument;
import com.freightos.pms.adapter.out.mart.document.PmsPerfDtEntryDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PmsBlMartDocument.lines[]/docs[]에서 sidecar 엔트리를 순수 파생(I/O 없음).
 *
 * line-accel ON일 때 ETL 워커가 적재 직전 호출한다.
 * lines/docs가 null이거나 빈 경우는 조용히 건너뛴다(line-accel OFF B/L 혼재 방어).
 */
final class PmsMartEntryDeriver {

    private PmsMartEntryDeriver() {}

    /**
     * B/L 문서 목록에서 pms_perfdt_entry 엔트리를 파생한다.
     * 각 (blKey, pd) 조합을 하나의 행으로 dedup한다.
     *
     * @param martDocs ETL에서 방금 upsert된 B/L 문서 배치
     * @return pms_perfdt_entry 적재 대상 목록
     */
    static List<PmsPerfDtEntryDocument> derivePerfDt(List<PmsBlMartDocument> martDocs) {
        List<PmsPerfDtEntryDocument> result = new ArrayList<>();

        for (PmsBlMartDocument doc : martDocs) {
            List<PmsBlLineEmbedded> lines = doc.getLines();
            if (lines == null || lines.isEmpty()) {
                continue;
            }

            // pd가 null/blank인 라인은 실적일자 sidecar 대상 외
            Map<String, List<PmsBlLineEmbedded>> byPd = lines.stream()
                .filter(l -> l.getPd() != null && !l.getPd().isBlank())
                .collect(Collectors.groupingBy(PmsBlLineEmbedded::getPd));

            for (Map.Entry<String, List<PmsBlLineEmbedded>> entry : byPd.entrySet()) {
                String pd = entry.getKey();
                List<PmsBlLineEmbedded> group = entry.getValue();

                boolean hasTaxIssued = group.stream().anyMatch(PmsBlLineEmbedded::isTax);
                boolean hasSlipIssued = group.stream().anyMatch(PmsBlLineEmbedded::isSlip);
                List<String> fdcTypes = group.stream()
                    .map(PmsBlLineEmbedded::getFdcType)
                    .filter(t -> t != null && !t.isBlank())
                    .distinct()
                    .collect(Collectors.toList());

                result.add(PmsPerfDtEntryDocument.builder()
                    .id(doc.getId() + "#" + pd)
                    .blKey(doc.getId())
                    .blId(doc.getBlId())
                    .blType(doc.getBlType())
                    .pd(pd)
                    .hasFreightInput(true)
                    .hasTaxIssued(hasTaxIssued)
                    .hasSlipIssued(hasSlipIssued)
                    .fdcTypes(fdcTypes)
                    .jobDiv(doc.getJobDiv())
                    .bound(doc.getBound())
                    .houseBlNo(doc.getHouseBlNo())
                    .masterBlNo(doc.getMasterBlNo())
                    .actualCustomerCode(doc.getActualCustomerCode())
                    .settlePartnerCode(doc.getSettlePartnerCode())
                    .linerCode(doc.getLinerCode())
                    .polCode(doc.getPolCode())
                    .podCode(doc.getPodCode())
                    .salesManCode(doc.getSalesManCode())
                    .incoterms(doc.getIncoterms())
                    .salesClass(doc.getSalesClass())
                    .houseTeamCode(doc.getHouseTeamCode())
                    .build());
            }
        }

        return result;
    }

    /**
     * B/L 문서 목록에서 pms_docdt_entry 엔트리를 파생한다.
     * financial_document 1건 = sidecar 1행 (fdId 기준 1:1).
     *
     * @param martDocs ETL에서 방금 upsert된 B/L 문서 배치
     * @return pms_docdt_entry 적재 대상 목록
     */
    static List<PmsDocDtEntryDocument> deriveDocDt(List<PmsBlMartDocument> martDocs) {
        List<PmsDocDtEntryDocument> result = new ArrayList<>();

        for (PmsBlMartDocument doc : martDocs) {
            List<PmsBlDocEmbedded> docs = doc.getDocs();
            if (docs == null || docs.isEmpty()) {
                continue;
            }

            for (PmsBlDocEmbedded d : docs) {
                result.add(PmsDocDtEntryDocument.builder()
                    .id(doc.getId() + "#" + d.getFdId())
                    .blKey(doc.getId())
                    .blId(doc.getBlId())
                    .blType(doc.getBlType())
                    .perfPd(d.getPerfPd())
                    .docDt(d.getDocDt())
                    .docType(d.getDocType())
                    .status(d.getStatus())
                    .grouped(d.isGrouped())
                    .teamCode(d.getTeam())
                    .operator(d.getOperator())
                    .jobDiv(doc.getJobDiv())
                    .bound(doc.getBound())
                    .houseBlNo(doc.getHouseBlNo())
                    .masterBlNo(doc.getMasterBlNo())
                    .actualCustomerCode(doc.getActualCustomerCode())
                    .settlePartnerCode(doc.getSettlePartnerCode())
                    .linerCode(doc.getLinerCode())
                    .polCode(doc.getPolCode())
                    .podCode(doc.getPodCode())
                    .salesManCode(doc.getSalesManCode())
                    .incoterms(doc.getIncoterms())
                    .salesClass(doc.getSalesClass())
                    .build());
            }
        }

        return result;
    }
}
