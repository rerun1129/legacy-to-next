package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.adapter.out.mart.cancel.PmsExactCountRegistry;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB Mart 기반 PmsPerformanceQueryPort 구현체.
 * pms.mart.enabled=true일 때만 등록된다.
 * 라우팅 결정은 PmsPerformanceQueryRouter가 담당하며 이 빈은 순수 Mart 조회만 수행한다.
 *
 * 날짜 필터 존재 + line-accel ON 시 2-tier 경로를 사용하며,
 * count 값으로 page 경로를 적응형 분기한다.
 *   - 밀집(count > earlyTermThreshold): keyset 우선(P1-a) / skip 폴백, count 캐시(P1-b)
 *     단, 경계 miss + offset > deepJumpOffsetThreshold 인 깊은 점프는 사이드카 경로 우회
 *   - 희소(count <= earlyTermThreshold): sidecar pageBlKeys + _id 조회
 * 그 외에는 fast path(Criteria 단일 쿼리)로 처리한다.
 *
 * page 선택 세부 로직은 PmsMartPageSelector에 위임한다.
 * count 결정 로직은 PmsMartCountResolver, keyset 페이지 선택은 PmsMartKeysetSupport가 담당한다.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartQueryAdapter implements PmsPerformanceQueryPort {

    private final PmsMartCriteriaBuilder criteriaBuilder;
    private final PmsMartPageCriteriaBuilder pageCriteriaBuilder;
    private final PmsMartRowMapper rowMapper;
    private final PmsMartCountResolver countResolver;
    private final PmsMartPageSelector pageSelector;

    /**
     * line-accel OFF이면 Optional.empty() — @RequiredArgsConstructor가 Optional<T> 파라미터를
     * 있는 경우 주입, 빈 없는 경우 Optional.empty로 자동 처리한다.
     */
    private final Optional<PmsMartDateDimQueryPlanner>   planner;
    private final Optional<PmsMartLineReaggregator>      reaggregator;
    private final Optional<PmsExactCountRegistry>        exactCountRegistry;

    @Override
    public Page<PmsRawBlRow> searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable) {
        String basisKey = switch (command.effectiveBasis()) {
            case FREIGHT_INPUT -> "freightInput";
            case TAX_ISSUED    -> "taxIssued";
            case SLIP_ISSUED   -> "slipIssued";
            // DOCUMENT_CREATED는 searchByDocument 경로 — 방어적 처리
            default -> throw new IllegalStateException("searchByFreightLine은 DOCUMENT_CREATED를 지원하지 않습니다: " + command.effectiveBasis());
        };
        String flagField = switch (command.effectiveBasis()) {
            case FREIGHT_INPUT -> "hasFreightInput";
            case TAX_ISSUED    -> "hasTaxIssued";
            case SLIP_ISSUED   -> "hasSlipIssued";
            default -> throw new IllegalStateException("지원하지 않는 basis: " + command.effectiveBasis());
        };

        // 어떤 경로(fast-path·2-tier)로 분기되든 직전 필터의 진행 중 정확 count를 먼저 취소한다.
        // 페이지 이동(동일 signature)은 레지스트리 내부 가드가 보호한다. line-accel OFF면 empty → no-op.
        String userKey = currentUserKey();
        String signature = PmsPerformanceFilterSignature.of(command);
        exactCountRegistry.ifPresent(r -> r.onNewSearch(userKey, signature));

        // 2-tier 경로: 실적일자 필터 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent() && hasFreightDate(command)) {
            String cacheKey = userKey + "|" + signature;
            Criteria pageCriteria = pageCriteriaBuilder.buildFreightPageCriteria(command, basisKey, flagField);
            long total = countResolver.resolveFreightTotal(command, flagField, pageCriteria, cacheKey, userKey, signature);
            List<PmsBlMartDocument> pageDocs = pageSelector.selectFreightPageDocs(command, flagField, pageCriteria, total, pageable, cacheKey);
            List<PmsRawBlRow> content = pageDocs.stream()
                .map(doc -> reaggregator.get().reaggregateFreight(doc, command, basisKey))
                .toList();
            return new PageImpl<>(content, pageable, total);
        }

        // fast path: count 캐시 + hint 정렬커버 인덱스 + keyset/skip
        String cacheKey = userKey + "|" + signature;
        Criteria criteria = criteriaBuilder.buildFreight(command, flagField);
        long total = countResolver.resolveFastPathTotal(criteria, cacheKey);
        Document hint = new Document(flagField, 1).append("blId", -1).append("blType", 1);
        List<PmsBlMartDocument> docs = pageSelector.selectDensePage(criteria, pageable, cacheKey, hint);
        List<PmsRawBlRow> content = docs.stream().map(doc -> rowMapper.toFreightRow(doc, basisKey)).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        // 어떤 경로(fast-path·2-tier)로 분기되든 직전 필터의 진행 중 정확 count를 먼저 취소한다.
        // 페이지 이동(동일 signature)은 레지스트리 내부 가드가 보호한다. line-accel OFF면 empty → no-op.
        String userKey = currentUserKey();
        String signature = PmsPerformanceFilterSignature.of(command);
        exactCountRegistry.ifPresent(r -> r.onNewSearch(userKey, signature));

        // 2-tier 경로: 날짜 필터(실적·서류 중 하나라도) 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent() && hasDocumentDate(command)) {
            String cacheKey = userKey + "|" + signature;
            Criteria docPageCriteria = pageCriteriaBuilder.buildDocumentPageCriteria(command);
            long total = countResolver.resolveDocumentTotal(command, docPageCriteria, cacheKey, userKey, signature);
            List<PmsBlMartDocument> pageDocs = pageSelector.selectDocumentPageDocs(command, docPageCriteria, total, pageable, cacheKey);
            List<PmsRawBlRow> content = pageDocs.stream()
                .map(doc -> reaggregator.get().reaggregateDocument(doc, command))
                .toList();
            return new PageImpl<>(content, pageable, total);
        }

        // fast path: count 캐시 + hint 정렬커버 인덱스 + keyset/skip
        String cacheKey = userKey + "|" + signature;
        Criteria criteria = criteriaBuilder.buildDocument(command);
        long total = countResolver.resolveFastPathTotal(criteria, cacheKey);
        Document hint = new Document("hasDocumentCreated", 1).append("blId", -1).append("blType", 1);
        List<PmsBlMartDocument> docs = pageSelector.selectDensePage(criteria, pageable, cacheKey, hint);
        List<PmsRawBlRow> content = docs.stream().map(doc -> rowMapper.toDocumentRow(doc)).toList();
        return new PageImpl<>(content, pageable, total);
    }

    // ── 캐시 키 생성 ─────────────────────────────────────────────────────────

    /** 인증 사용자 키. 미인증 또는 null이면 "anonymous"를 반환한다. */
    private static String currentUserKey() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return "anonymous";
        return auth.getName();
    }

    // ── 날짜 존재 헬퍼 ────────────────────────────────────────────────────────

    /** freight basis: 실적일자 필터가 하나라도 있으면 2-tier 대상. */
    private static boolean hasFreightDate(SearchPmsPerformanceCommand c) {
        return StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo());
    }

    /** document basis: 실적일자 또는 서류일자 필터가 하나라도 있으면 2-tier 대상. */
    private static boolean hasDocumentDate(SearchPmsPerformanceCommand c) {
        return hasFreightDate(c)
            || StringUtils.hasText(c.documentDtFrom())
            || StringUtils.hasText(c.documentDtTo());
    }

}
