package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlLineEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * line-grain 임베드 배열(lines/docs)로 페이지 단위 금액을 재집계한다.
 *
 * pms.mart.line-accel.enabled=true일 때만 등록된다.
 * OFF 경로(Optional.empty)는 PmsMartQueryAdapter에서 fast-path로 fall-through한다.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart.line-accel", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartLineReaggregator {

    private final PmsMartRowMapper rowMapper;

    // ── 공개 API ─────────────────────────────────────────────────────────────

    /**
     * freight_line 임베드 배열(lines[])로 페이지 금액을 재집계한다.
     * 실적일자 범위가 있으면 inRange로, 없으면(ETD/ETA·무날짜 진입) 전 라인을 basis/docType/issued로만 필터해 합산한다.
     *
     * @param basisKey "freightInput" | "taxIssued" | "slipIssued"
     */
    public PmsRawBlRow reaggregateFreight(
            PmsBlMartDocument doc,
            SearchPmsPerformanceCommand c,
            String basisKey) {

        List<PmsBlLineEmbedded> lines = doc.getLines() != null ? doc.getLines() : List.of();

        // ETD/ETA·무날짜로 2-tier 진입 시 실적일자 범위가 없다. 이때 inRange(pd,null,null)는
        // pd=null 라인을 누락시켜 금액이 틀어지므로, 실적일자 범위가 있을 때만 inRange를 적용한다.
        // (OLTP도 performanceDt 술어가 없으면 전 라인을 합산 → 동치)
        boolean hasPerfDtRange = StringUtils.hasText(c.performanceDtFrom())
            || StringUtils.hasText(c.performanceDtTo());

        List<PmsBlLineEmbedded> matched = lines.stream()
            .filter(line -> !hasPerfDtRange || inRange(line.getPd(), c.performanceDtFrom(), c.performanceDtTo()))
            .filter(line -> matchesBasisFlag(line, basisKey))
            .filter(line -> matchesDocType(line.getFdcType(), c))
            .filter(line -> matchesIssued(line, c))
            .toList();

        BigDecimal[] sums = sumLineAmounts(matched);
        String perfDt = matched.stream()
            .map(PmsBlLineEmbedded::getPd)
            .filter(StringUtils::hasText)
            .max(Comparator.naturalOrder())
            .orElse(null);

        // freight 경로: HOUSE 전용 teamCode/teamName, MASTER는 null, operator 없음
        boolean isHouse = "HOUSE".equals(doc.getBlType());
        String teamCode = isHouse ? doc.getHouseTeamCode() : null;
        String teamName = isHouse ? doc.getHouseTeamName() : null;

        return rowMapper.buildReaggregated(doc, perfDt,
            sums[0], sums[1], sums[2], sums[3],
            sums[4], sums[5], sums[6], sums[7],
            teamCode, teamName, null);
    }

    /**
     * financial_document 임베드 배열(docs[])로 페이지 금액을 재집계한다.
     * docs[]는 이미 distinct fd 단위이므로 fan-out 없음.
     */
    public PmsRawBlRow reaggregateDocument(
            PmsBlMartDocument doc,
            SearchPmsPerformanceCommand c) {

        List<PmsBlDocEmbedded> docs = doc.getDocs() != null ? doc.getDocs() : List.of();

        List<PmsBlDocEmbedded> matched = docs.stream()
            .filter(d -> matchesDocumentDateFilters(d, c))
            .filter(d -> matchesDocumentTypes(d.getDocType(), c))
            .filter(d -> matchesDocumentStatus(d.getStatus(), c))
            .filter(d -> matchesGrouped(d, c))
            .filter(d -> matchesTeamAndOperator(d, c))
            .toList();

        BigDecimal[] sums = sumDocAmounts(matched);
        String perfDt = matched.stream()
            .map(PmsBlDocEmbedded::getPerfPd)
            .filter(StringUtils::hasText)
            .max(Comparator.naturalOrder())
            .orElse(null);

        // OLTP max(team_code)/max(operator) + 팀명 조인과 동치
        String teamCode = matched.stream()
            .map(PmsBlDocEmbedded::getTeam)
            .filter(StringUtils::hasText)
            .max(Comparator.naturalOrder())
            .orElse(null);
        String teamName = resolveTeamName(matched, teamCode);
        String operator = matched.stream()
            .map(PmsBlDocEmbedded::getOperator)
            .filter(StringUtils::hasText)
            .max(Comparator.naturalOrder())
            .orElse(null);

        return rowMapper.buildReaggregated(doc, perfDt,
            sums[0], sums[1], sums[2], sums[3],
            sums[4], sums[5], sums[6], sums[7],
            teamCode, teamName, operator);
    }

    // ── 날짜 범위 헬퍼 ────────────────────────────────────────────────────────

    /**
     * yyyyMMdd 문자열 사전순 비교로 범위 검사한다.
     * v가 null/blank이면 false; from 있으면 v >= from; to 있으면 v <= to.
     */
    private static boolean inRange(String v, String from, String to) {
        if (!StringUtils.hasText(v)) return false;
        if (StringUtils.hasText(from) && v.compareTo(from) < 0) return false;
        if (StringUtils.hasText(to)   && v.compareTo(to)   > 0) return false;
        return true;
    }

    // ── freight 필터 헬퍼 ────────────────────────────────────────────────────

    private static boolean matchesBasisFlag(PmsBlLineEmbedded line, String basisKey) {
        return switch (basisKey) {
            case "freightInput" -> true;
            case "taxIssued"    -> line.isTax();
            case "slipIssued"   -> line.isSlip();
            default -> throw new IllegalArgumentException("지원하지 않는 basisKey: " + basisKey);
        };
    }

    private static boolean matchesDocType(String fdcType, SearchPmsPerformanceCommand c) {
        List<String> types = c.documentTypes();
        if (types != null && !types.isEmpty()) return types.contains(fdcType);
        if (StringUtils.hasText(c.financialDocType())) return c.financialDocType().equals(fdcType);
        return true;
    }

    private static boolean matchesIssued(PmsBlLineEmbedded line, SearchPmsPerformanceCommand c) {
        String issued = c.issued();
        if (!StringUtils.hasText(issued)) return true;
        return switch (issued) {
            case "Y" -> line.isIssued();
            case "N" -> !line.isIssued();
            default  -> true;
        };
    }

    // ── document 필터 헬퍼 ──────────────────────────────────────────────────

    private static boolean matchesDocumentDateFilters(PmsBlDocEmbedded d, SearchPmsPerformanceCommand c) {
        if (StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo())) {
            if (!inRange(d.getPerfPd(), c.performanceDtFrom(), c.performanceDtTo())) return false;
        }
        if (StringUtils.hasText(c.documentDtFrom()) || StringUtils.hasText(c.documentDtTo())) {
            if (!inRange(d.getDocDt(), c.documentDtFrom(), c.documentDtTo())) return false;
        }
        return true;
    }

    private static boolean matchesDocumentTypes(String docType, SearchPmsPerformanceCommand c) {
        List<String> types = c.documentTypes();
        if (types == null || types.isEmpty()) return true;
        return types.contains(docType);
    }

    private static boolean matchesDocumentStatus(String status, SearchPmsPerformanceCommand c) {
        if (!StringUtils.hasText(c.documentStatus())) return true;
        return c.documentStatus().equals(status);
    }

    private static boolean matchesGrouped(PmsBlDocEmbedded d, SearchPmsPerformanceCommand c) {
        String grouped = c.grouped();
        if (!StringUtils.hasText(grouped)) return true;
        return switch (grouped) {
            case "Y" -> d.isGrouped();
            case "N" -> !d.isGrouped();
            default  -> true;
        };
    }

    private static boolean matchesTeamAndOperator(PmsBlDocEmbedded d, SearchPmsPerformanceCommand c) {
        if (StringUtils.hasText(c.teamCode()) && !c.teamCode().equals(d.getTeam())) return false;
        if (StringUtils.hasText(c.operator()) && !c.operator().equals(d.getOperator())) return false;
        return true;
    }

    // ── 금액 합산 헬퍼 ────────────────────────────────────────────────────────

    /**
     * lines 합산 결과를 [invL, debL, payL, crdL, invU, debU, payU, crdU] 순서로 반환한다.
     * null은 ZERO로 취급한다.
     */
    private static BigDecimal[] sumLineAmounts(List<PmsBlLineEmbedded> lines) {
        BigDecimal invL = BigDecimal.ZERO, debL = BigDecimal.ZERO,
                   payL = BigDecimal.ZERO, crdL = BigDecimal.ZERO;
        BigDecimal invU = BigDecimal.ZERO, debU = BigDecimal.ZERO,
                   payU = BigDecimal.ZERO, crdU = BigDecimal.ZERO;

        for (PmsBlLineEmbedded line : lines) {
            BigDecimal local = orZero(line.getLocal());
            BigDecimal usd   = orZero(line.getUsd());
            switch (line.getFdcType() != null ? line.getFdcType() : "") {
                case "INVOICE" -> { invL = invL.add(local); invU = invU.add(usd); }
                case "DEBIT"   -> { debL = debL.add(local); debU = debU.add(usd); }
                case "PAYMENT" -> { payL = payL.add(local); payU = payU.add(usd); }
                case "CREDIT"  -> { crdL = crdL.add(local); crdU = crdU.add(usd); }
                default -> { /* 알 수 없는 타입은 무시 */ }
            }
        }
        return new BigDecimal[]{ invL, debL, payL, crdL, invU, debU, payU, crdU };
    }

    /**
     * docs 합산 결과를 [invL, debL, payL, crdL, invU, debU, payU, crdU] 순서로 반환한다.
     * null은 ZERO로 취급한다.
     */
    private static BigDecimal[] sumDocAmounts(List<PmsBlDocEmbedded> docs) {
        BigDecimal invL = BigDecimal.ZERO, debL = BigDecimal.ZERO,
                   payL = BigDecimal.ZERO, crdL = BigDecimal.ZERO;
        BigDecimal invU = BigDecimal.ZERO, debU = BigDecimal.ZERO,
                   payU = BigDecimal.ZERO, crdU = BigDecimal.ZERO;

        for (PmsBlDocEmbedded d : docs) {
            BigDecimal local = orZero(d.getLocal());
            BigDecimal usd   = orZero(d.getUsd());
            switch (d.getDocType() != null ? d.getDocType() : "") {
                case "INVOICE" -> { invL = invL.add(local); invU = invU.add(usd); }
                case "DEBIT"   -> { debL = debL.add(local); debU = debU.add(usd); }
                case "PAYMENT" -> { payL = payL.add(local); payU = payU.add(usd); }
                case "CREDIT"  -> { crdL = crdL.add(local); crdU = crdU.add(usd); }
                default -> { /* 알 수 없는 타입은 무시 */ }
            }
        }
        return new BigDecimal[]{ invL, debL, payL, crdL, invU, debU, payU, crdU };
    }

    // ── 팀명 해소 ────────────────────────────────────────────────────────────

    /**
     * max(team_code) 값과 일치하는 첫 번째 doc 원소의 teamName을 반환한다.
     * OLTP "max(team_code) JOIN admin.team"과 동치.
     */
    private static String resolveTeamName(List<PmsBlDocEmbedded> matched, String maxTeamCode) {
        if (!StringUtils.hasText(maxTeamCode)) return null;
        return matched.stream()
            .filter(d -> maxTeamCode.equals(d.getTeam()))
            .map(PmsBlDocEmbedded::getTeamName)
            .filter(StringUtils::hasText)
            .findFirst()
            .orElse(null);
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
