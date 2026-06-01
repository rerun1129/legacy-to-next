package com.freightos.fms.adapter.in.web.blquicksearch.dto;

import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import org.springframework.util.StringUtils;

/**
 * BL 자동완성 응답 아이템.
 * label 포맷: "POL→POD · ETD - ETA" (구분자 " · ", 화살표 "→", 날짜 범위 " - ", null/공백 슬롯은 생략).
 */
public record BlQuickSearchItemResponse(
    Long id,
    String blType,
    String blNo,
    String jobDiv,
    String bound,
    String shipperCode,
    String polCode,
    String podCode,
    String etd,
    String label
) {

    public static BlQuickSearchItemResponse from(BlQuickSearchSummary summary) {
        return new BlQuickSearchItemResponse(
                summary.id(),
                summary.blType(),
                summary.blNo(),
                summary.jobDiv(),
                summary.bound(),
                summary.shipperCode(),
                summary.polCode(),
                summary.podCode(),
                summary.etd(),
                buildLabel(summary)
        );
    }

    private static String buildLabel(BlQuickSearchSummary s) {
        StringBuilder sb = new StringBuilder();
        String route = buildRoute(s.polCode(), s.podCode());
        if (StringUtils.hasText(route)) {
            sb.append(route);
        }
        String dateRange = buildDateRange(s.etd(), s.eta());
        if (StringUtils.hasText(dateRange)) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(dateRange);
        }
        return sb.toString();
    }

    private static String buildDateRange(String etd, String eta) {
        boolean hasEtd = StringUtils.hasText(etd);
        boolean hasEta = StringUtils.hasText(eta);
        if (hasEtd && hasEta) return etd + " - " + eta;
        if (hasEtd) return etd;
        if (hasEta) return eta;
        return "";
    }

    private static String buildRoute(String pol, String pod) {
        boolean hasPol = StringUtils.hasText(pol);
        boolean hasPod = StringUtils.hasText(pod);
        if (!hasPol && !hasPod) return "";
        if (hasPol && hasPod) return pol + "→" + pod;
        return hasPol ? pol : pod;
    }
}
