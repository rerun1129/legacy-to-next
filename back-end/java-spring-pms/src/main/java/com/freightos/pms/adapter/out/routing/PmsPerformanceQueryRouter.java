package com.freightos.pms.adapter.out.routing;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.PmsMartFilterSupport;
import com.freightos.pms.adapter.out.mart.PmsMartQueryAdapter;
import com.freightos.pms.adapter.out.persistence.pms.PmsPerformancePersistenceAdapter;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

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

    @Override
    public Page<PmsRawBlRow> searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable) {
        return useMart(command)
            ? mart.searchByFreightLine(command, pageable)
            : oltp.searchByFreightLine(command, pageable);
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        return useMart(command)
            ? mart.searchByDocument(command, pageable)
            : oltp.searchByDocument(command, pageable);
    }

    private boolean useMart(SearchPmsPerformanceCommand command) {
        return props.isMartOnly() || filterSupport.supportedByMart(command);
    }
}
