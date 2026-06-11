package com.freightos.pms.adapter.in.web.mart;

import com.freightos.common.response.ApiResponse;
import com.freightos.pms.adapter.in.web.mart.dto.MartRebuildResponse;
import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import com.freightos.pms.application.mart.port.in.PmsMartMaintenanceUseCase;
import com.freightos.pms.application.mart.result.MartSyncResult;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mart 관리 REST 컨트롤러.
 * pms.mart.enabled=true일 때만 등록된다.
 * SecurityConfig의 .requestMatchers("/api/**").authenticated() 규칙을 상속하므로 별도 설정 불필요.
 *
 * rebuild 엔드포인트는 pmsMartRebuild RateLimiter(1회/60s)로 보호한다.
 * /status는 읽기 전용이므로 RateLimiter 비대상이다.
 */
@RestController
@RequestMapping("/api/pms/mart")
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
public class PmsMartAdminController {

    private static final String REBUILD_RATE_LIMITER = "pmsMartRebuild";

    private final PmsMartMaintenanceUseCase maintenanceUseCase;
    private final RateLimiter rebuildLimiter;

    public PmsMartAdminController(PmsMartMaintenanceUseCase maintenanceUseCase,
                                   RateLimiterRegistry rateLimiterRegistry) {
        this.maintenanceUseCase = maintenanceUseCase;
        this.rebuildLimiter = rateLimiterRegistry.rateLimiter(REBUILD_RATE_LIMITER);
    }

    /**
     * POST /api/pms/mart/rebuild?mode=full|incremental
     * 기본값: mode=full.
     * pmsMartRebuild RateLimiter(1회/60s) 초과 시 RequestNotPermitted → GlobalExceptionHandler가 429로 변환.
     */
    @PostMapping("/rebuild")
    public ApiResponse<MartRebuildResponse> rebuild(
            @RequestParam(defaultValue = "full") String mode) {
        MartSyncResult result = rebuildLimiter.executeSupplier(() -> switch (mode) {
            case "incremental" -> maintenanceUseCase.rebuildIncremental();
            default -> maintenanceUseCase.rebuildFull();
        });
        return ApiResponse.of(MartRebuildResponse.from(result));
    }

    /** GET /api/pms/mart/status — 현재 동기화 상태 조회. RateLimiter 비대상. */
    @GetMapping("/status")
    public ApiResponse<PmsMartSyncState> status() {
        return ApiResponse.of(maintenanceUseCase.status());
    }
}
