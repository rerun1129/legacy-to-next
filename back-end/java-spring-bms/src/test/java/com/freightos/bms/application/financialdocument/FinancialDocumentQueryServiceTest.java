package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentSearchPort;
import com.freightos.bms.application.port.out.CodeNameResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FinancialDocumentQueryServiceTest {

    @Mock
    private FinancialDocumentSearchPort searchPort;

    @Mock
    private CodeNameResolver codeNameResolver;

    @InjectMocks
    private FinancialDocumentQueryService queryService;

    private SearchFinancialDocumentCriteria criteria;

    @BeforeEach
    void setUp() {
        criteria = new SearchFinancialDocumentCriteria(
            List.of("INVOICE"), null, "CUST001", null, null, "operator1",
            null, null, null, null, null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("search — customerName·teamName·operatorName이 CodeNameResolver에서 resolve된다")
    void search_resolvesNames() {
        FinancialDocumentSearchView rawView = new FinancialDocumentSearchView(
            1L, "INV-2406-00001", "INVOICE", "20240601", "CREATED",
            "CUST001", "",
            BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            "20240601", "SEA", "", "operator1", "", null,
            "HOUSE", "10", "SEA", "EXP", "HBL-001", "20240601", "20240610"
        );
        Page<FinancialDocumentSearchView> rawPage = new PageImpl<>(List.of(rawView), PageRequest.of(0, 20), 1);

        given(searchPort.search(any(), any())).willReturn(rawPage);
        given(codeNameResolver.findCustomerNames(anyCollection())).willReturn(Map.of("CUST001", "고객법인"));
        given(codeNameResolver.findTeamNames(anyCollection())).willReturn(Map.of("SEA", "해상팀"));
        given(codeNameResolver.findOperatorNames(anyCollection())).willReturn(Map.of("operator1", "홍길동"));

        Page<FinancialDocumentSearchView> result = queryService.search(criteria, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        FinancialDocumentSearchView resolved = result.getContent().get(0);
        assertThat(resolved.customerName()).isEqualTo("고객법인");
        assertThat(resolved.teamName()).isEqualTo("해상팀");
        assertThat(resolved.operatorName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("search — 빈 결과 페이지는 CodeNameResolver 호출 없이 즉시 반환된다")
    void search_emptyPage_noResolverCall() {
        given(searchPort.search(any(), any())).willReturn(Page.empty());

        Page<FinancialDocumentSearchView> result = queryService.search(criteria, PageRequest.of(0, 20));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("findDocumentLines — customerName이 CodeNameResolver에서 resolve된다")
    void findDocumentLines_resolvesCustomerName() {
        FreightLineDetailView rawLine = new FreightLineDetailView(
            10L, 5L, "SELLING", "INVOICE", "FRT001", "",
            BigDecimal.ONE, BigDecimal.valueOf(1000), "BL",
            "USD", BigDecimal.valueOf(1300), BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1300000), BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.valueOf(0.000769), BigDecimal.valueOf(769.23),
            "CUST001", "",
            "TAXABLE", null, null, null, null, "20240601", 1L
        );

        given(searchPort.findLinesByDocument(eq(1L))).willReturn(List.of(rawLine));
        given(codeNameResolver.findCustomerNames(anyCollection())).willReturn(Map.of("CUST001", "고객법인"));

        List<FreightLineDetailView> result = queryService.findDocumentLines(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).customerName()).isEqualTo("고객법인");
        // freightName은 현재 미구현으로 빈 문자열
        assertThat(result.get(0).freightName()).isEmpty();
    }

    @Test
    @DisplayName("findDocumentLines — 빈 결과는 즉시 반환된다")
    void findDocumentLines_emptyResult() {
        given(searchPort.findLinesByDocument(eq(99L))).willReturn(List.of());

        List<FreightLineDetailView> result = queryService.findDocumentLines(99L);

        assertThat(result).isEmpty();
    }
}
