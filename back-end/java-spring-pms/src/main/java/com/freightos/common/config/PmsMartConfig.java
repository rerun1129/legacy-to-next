package com.freightos.common.config;

import com.mongodb.MongoSocketException;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * PMS Mart 빈군 활성화 설정.
 * pms.mart.enabled=true 일 때만 이 설정 클래스가 로드되며,
 * Mart 라우터 · 어댑터 · 스케줄러가 함께 등록된다.
 * 기본값(enabled 미설정 또는 false)에서는 OLTP 어댑터가 그대로 동작한다.
 */
@Configuration
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(PmsMartProperties.class)
@EnableScheduling
public class PmsMartConfig {

    /**
     * Mongo 클라이언트 server-selection·connect 타임아웃을 하향 조정한다.
     * socket read 타임아웃은 변경하지 않는다 — 긴 exact-count 집계·full rebuild가 죽으면 안 됨.
     * 타임아웃을 줄이면 Mongo 다운 시 회로차단기가 빠르게 열려 OLTP 폴백으로 전환된다.
     */
    @Bean
    public MongoClientSettingsBuilderCustomizer pmsMartMongoTimeouts(PmsMartProperties props) {
        long serverSelection = props.getMongo().getServerSelectionTimeoutMs();
        int connect = props.getMongo().getConnectTimeoutMs();
        return builder -> builder
                .applyToClusterSettings(c -> c.serverSelectionTimeout(serverSelection, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(s -> s.connectTimeout(connect, TimeUnit.MILLISECONDS));
        // socket read 타임아웃을 안 거는 이유:
        // 공유 MongoClient라 ETL createIndex(3M행)·count maxTimeMS 30s 백스톱과 충돌하기 때문이다.
        // 대신 TimeLimiter(15s)로 클라이언트 스레드를 보호하고, maxTime 주입으로 서버측 kill을 보장한다.
    }

    /**
     * Mart 조회 전용 고정 스레드 풀 executor.
     *
     * (a) SecurityContext 전파 사유:
     *     PmsMartQueryAdapter.currentUserKey()가 SecurityContextHolder(ThreadLocal)를 읽는다.
     *     DelegatingSecurityContextExecutorService 미래핑 시 submit 스레드에서 "anonymous"로 조회되어
     *     exact-count 취소 레지스트리 교차 kill + 캐시 키 충돌이 발생한다.
     *
     * (b) destroyMethod=shutdownNow 사유:
     *     JDK ExecutorService.close()는 shutdown() + 무한 awaitTermination이므로
     *     소켓 read 중인 Mongo 워커가 종료되지 않으면 애플리케이션 셧다운이 행(hang)한다.
     *     shutdownNow()는 인터럽트를 보내 최대한 빠르게 종료를 시도한다.
     */
    @Bean(name = "pmsMartQueryExecutor", destroyMethod = "shutdownNow")
    public ExecutorService pmsMartQueryExecutor() {
        AtomicInteger counter = new AtomicInteger(0);
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "pms-mart-query-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        ExecutorService delegate = Executors.newFixedThreadPool(8, factory);
        return new DelegatingSecurityContextExecutorService(delegate);
    }

    /**
     * Retry "pmsMart" 인스턴스에 retryableMartFault 술어를 등록한다.
     *
     * ⚠️ customizer 안에서 ignoreExceptions() 재호출 금지:
     *     builder.ignoreExceptions()를 호출하면 yml의 ignoreExceptions 배열이 대체된다.
     *     yml 선언이 가독성 목적의 이중 안전망으로 공존하도록 retryOnException만 설정한다.
     */
    @Bean
    public RetryConfigCustomizer pmsMartRetryCustomizer() {
        Predicate<Throwable> pred = PmsMartConfig::retryableMartFault;
        return RetryConfigCustomizer.of("pmsMart", builder -> builder.retryOnException(pred));
    }

    /**
     * Retry 재시도 대상 판별 술어.
     *
     * cause-chain을 걸어 MongoSocketException(하위 포함)이 있으면 true를 반환한다.
     * MongoSocketException 하위: MongoSocketReadException, MongoSocketOpenException,
     *   MongoSocketReadTimeoutException, MongoSocketWriteException 등 일시 socket 결함
     *
     * MongoTimeoutException(serverSelection 타임아웃) = MongoClientException 하위라
     * MongoSocketException과 다른 계층 → 비재시도.
     * 이유: serverSelection 타임아웃은 Mongo가 완전 다운된 상태를 의미하므로
     * 재시도 해봤자 3s × n회 추가 대기만 발생하고 결국 OLTP 폴백으로 끝난다.
     *
     * public static: 동일 패키지 진리표 테스트(PmsMartRetryPredicateTest) 및 타 패키지 가드 테스트들이 운영과 동일한 술어로 RetryConfig를 구성할 때 직접 참조한다.
     */
    public static boolean retryableMartFault(Throwable t) {
        Throwable current = t;
        int depth = 0;
        while (current != null && depth < 20) {
            if (current instanceof MongoSocketException) {
                return true;
            }
            Throwable cause = current.getCause();
            // 자기참조 cause 무한루프 가드
            if (cause == current) break;
            current = cause;
            depth++;
        }
        return false;
    }

}
