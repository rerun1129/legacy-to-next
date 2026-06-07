package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.common.config.PmsMartProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mart 증분 동기화 스케줄러.
 * pms.mart.scheduler.enabled=true일 때만 등록된다.
 *
 * CronTrigger 기반 nextExecution에 jitter(임의 지연)를 더해 여러 인스턴스 동시 기동을 분산한다.
 * ETL이 이미 running 중이면 skip한다(AtomicBoolean 차단).
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart.scheduler", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class PmsMartScheduler implements SchedulingConfigurer {

    private final PmsMartEtlService etlService;
    private final PmsMartProperties props;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        PmsMartProperties.Scheduler schedulerProps = props.getScheduler();
        CronTrigger cronTrigger = new CronTrigger(schedulerProps.getCron());
        long jitterMaxMs = schedulerProps.getJitterMaxMs();

        registrar.addTriggerTask(
            this::runIncremental,
            triggerContext -> {
                Instant next = cronTrigger.nextExecution(triggerContext);
                if (next == null) {
                    return null;
                }
                long jitterMs = ThreadLocalRandom.current().nextLong(0, jitterMaxMs + 1);
                return next.plusMillis(jitterMs);
            }
        );
    }

    private void runIncremental() {
        try {
            etlService.rebuildIncremental();
        } catch (IllegalStateException e) {
            // 이미 실행 중 — 정상적인 skip
            log.info("Mart 스케줄러 skip: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Mart 스케줄러 증분 동기화 실패: {}", e.getMessage(), e);
        }
    }
}
