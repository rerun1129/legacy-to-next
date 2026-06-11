package com.freightos.pms.adapter.out.routing;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.PmsMartFilterSupport;
import com.freightos.pms.adapter.out.mart.PmsMartQueryAdapter;
import com.freightos.pms.adapter.out.persistence.pms.PmsPerformancePersistenceAdapter;
import com.freightos.pms.application.pms.PmsRawBlSearchResult;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * PmsPerformanceQueryPort 라우팅 어댑터.
 *
 * enabled=true면 @Primary로 포트 주입을 가져가고 두 구체 어댑터를 위임한다.
 * enabled=false면 이 빈·Mart 어댑터 미생성 → 기존 PmsPerformancePersistenceAdapter가
 * 유일 포트 빈이 되어 오늘과 동일하게 동작한다.
 *
 * 라우팅 전략:
 * - isMartOnly()==true → 항상 Mart
 * - filterSupport.supportedByMart()==true → Mart
 * - 그 외 → OLTP 폴백
 * - Mart 결함 시 회로차단기로 OLTP 폴백
 *
 * OLTP supplier는 직행·폴백 양 경로 모두 PmsOltpBulkheadGuard로 래핑한다.
 * CB OPEN 시 모든 트래픽이 Postgres(Hikari 10)로 몰려 커넥션 풀이 고갈되는 것을 방지한다.
 */
@Primary
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsPerformanceQueryRouter implements PmsPerformanceQueryPort {

    private final PmsPerformancePersistenceAdapter oltp;
    private final PmsMartQueryAdapter mart;
    private final PmsMartFilterSupport filterSupport;
    private final PmsMartProperties props;
    private final PmsMartCircuitGuard guard;
    private final PmsOltpBulkheadGuard oltpBulkhead;

    @Override
    public PmsRawBlSearchResult searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable) {
        Supplier<PmsRawBlSearchResult> oltpCall =
                () -> oltpBulkhead.execute(() -> oltp.searchByFreightLine(command, pageable));
        if (!useMart(command)) return oltpCall.get();
        return guard.martOrOltp(() -> mart.searchByFreightLine(command, pageable), oltpCall);
    }

    @Override
    public PmsRawBlSearchResult searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        Supplier<PmsRawBlSearchResult> oltpCall =
                () -> oltpBulkhead.execute(() -> oltp.searchByDocument(command, pageable));
        if (!useMart(command)) return oltpCall.get();
        return guard.martOrOltp(() -> mart.searchByDocument(command, pageable), oltpCall);
    }

    private boolean useMart(SearchPmsPerformanceCommand command) {
        return props.isMartOnly() || filterSupport.supportedByMart(command);
    }
}
