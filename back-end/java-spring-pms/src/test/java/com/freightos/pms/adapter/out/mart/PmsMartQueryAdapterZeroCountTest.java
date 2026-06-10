package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.cancel.PmsExactCountRegistry;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.PmsRawBlSearchResult;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PmsMartQueryAdapter total=0 시 페이지 find 생략 단위 테스트.
 *
 * 버그 B 수정 검증:
 * - count(total)=0이면 mongoTemplate.find()가 호출되지 않고 빈 Page(total=0)가 반환되는지 확인한다.
 * - fast-path, 2-tier freight, 2-tier document 경로 각각에 대해 검증한다.
 *
 * 라이브 MongoDB 없이 Mockito mock만 사용한다.
 * 시간·랜덤·sleep 의존 없는 결정적 로직만 사용한다.
 */
class PmsMartQueryAdapterZeroCountTest {

    private MongoTemplate mongoTemplate;
    private PmsMartCriteriaBuilder criteriaBuilder;
    private PmsMartPageCriteriaBuilder pageCriteriaBuilder;
    private PmsMartRowMapper rowMapper;
    private PmsMartCountResolver countResolver;

    private PmsMartQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        mongoTemplate       = mock(MongoTemplate.class);
        criteriaBuilder     = mock(PmsMartCriteriaBuilder.class);
        pageCriteriaBuilder = mock(PmsMartPageCriteriaBuilder.class);
        rowMapper           = mock(PmsMartRowMapper.class);
        countResolver       = mock(PmsMartCountResolver.class);
        PmsMartProperties props = new PmsMartProperties();

        // line-accel OFF(planner·reaggregator·exactCountRegistry 없음)
        adapter = new PmsMartQueryAdapter(
            mongoTemplate,
            criteriaBuilder,
            pageCriteriaBuilder,
            rowMapper,
            props,
            countResolver,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );

        // SecurityContext 초기화 — currentUserKey() 호출 시 NPE 방지
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("testUser", null, List.of())
        );
    }

    // ── fast-path: total=0 시 find 생략 ──────────────────────────────────────

    @Test
    void fastPath_total이0이면_find가호출되지않고_빈Page를반환한다() {
        SearchPmsPerformanceCommand cmd = freightInputCmdWithNoFilter();
        Pageable pageable = PageRequest.of(0, 20);
        Criteria fakeCriteria = Criteria.where("hasFreightInput").is(true);

        when(criteriaBuilder.buildFreight(eq(cmd), any())).thenReturn(fakeCriteria);
        when(countResolver.resolveFastPathTotal(eq(fakeCriteria), eq(cmd), any())).thenReturn(ResolvedTotal.exact(0L));

        PmsRawBlSearchResult result = adapter.searchByFreightLine(cmd, pageable);

        assertThat(result.page().getTotalElements()).isEqualTo(0L);
        assertThat(result.page().getContent()).isEmpty();
        // total=0이면 find는 호출되지 않아야 한다
        verify(mongoTemplate, never()).find(any(Query.class), eq(PmsBlMartDocument.class));
    }

    @Test
    void fastPath_total이0이면_빈Page의content도비어있다() {
        SearchPmsPerformanceCommand cmd = taxIssuedCmdWithNoFilter();
        Pageable pageable = PageRequest.of(0, 20);
        Criteria fakeCriteria = Criteria.where("hasTaxIssued").is(true);

        when(criteriaBuilder.buildFreight(eq(cmd), any())).thenReturn(fakeCriteria);
        when(countResolver.resolveFastPathTotal(eq(fakeCriteria), eq(cmd), any())).thenReturn(ResolvedTotal.exact(0L));

        PmsRawBlSearchResult result = adapter.searchByFreightLine(cmd, pageable);

        assertThat(result.page().getContent()).isEmpty();
        assertThat(result.page().getPageable()).isEqualTo(pageable);
        verify(mongoTemplate, never()).find(any(Query.class), eq(PmsBlMartDocument.class));
    }

    @Test
    void fastPath_document_total이0이면_find가호출되지않고_빈Page를반환한다() {
        SearchPmsPerformanceCommand cmd = documentCreatedCmdWithNoFilter();
        Pageable pageable = PageRequest.of(0, 20);
        Criteria fakeCriteria = Criteria.where("hasDocCreated").is(true);

        when(criteriaBuilder.buildDocument(eq(cmd))).thenReturn(fakeCriteria);
        when(countResolver.resolveFastPathTotal(eq(fakeCriteria), eq(cmd), any())).thenReturn(ResolvedTotal.exact(0L));

        PmsRawBlSearchResult result = adapter.searchByDocument(cmd, pageable);

        assertThat(result.page().getTotalElements()).isEqualTo(0L);
        assertThat(result.page().getContent()).isEmpty();
        verify(mongoTemplate, never()).find(any(Query.class), eq(PmsBlMartDocument.class));
    }

    // ── 보조 커맨드 빌더 헬퍼 ────────────────────────────────────────────────

    /** FREIGHT_INPUT, 날짜/차원 필터 없음 — fast-path 진입 커맨드. */
    private SearchPmsPerformanceCommand freightInputCmdWithNoFilter() {
        return new SearchPmsPerformanceCommand(
            AggregationBasis.FREIGHT_INPUT, 0, 20,
            null, null, null, null, null,
            null, null, null, null,
            null, null, null, null
        );
    }

    /** TAX_ISSUED, 날짜/차원 필터 없음 — fast-path 진입 커맨드. */
    private SearchPmsPerformanceCommand taxIssuedCmdWithNoFilter() {
        return new SearchPmsPerformanceCommand(
            AggregationBasis.TAX_ISSUED, 0, 20,
            null, null, null, null, null,
            null, null, null, null,
            null, null, null, null
        );
    }

    /** DOCUMENT_CREATED, 날짜/차원 필터 없음 — fast-path 진입 커맨드. */
    private SearchPmsPerformanceCommand documentCreatedCmdWithNoFilter() {
        return new SearchPmsPerformanceCommand(
            AggregationBasis.DOCUMENT_CREATED, 0, 20,
            null, null, null, null, null,
            null, null, null, null,
            null, null, null, null
        );
    }
}
