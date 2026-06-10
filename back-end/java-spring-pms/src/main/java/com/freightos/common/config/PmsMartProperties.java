package com.freightos.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * pms.mart 설정 블록 바인딩 POJO.
 * @EnableConfigurationProperties(PmsMartProperties.class)로 등록되며
 * 이 클래스 자체에 @Component를 붙이지 않는다.
 */
@ConfigurationProperties(prefix = "pms.mart")
@Getter
@Setter
public class PmsMartProperties {

    /** false(기본)이면 OLTP 어댑터만 동작, Mongo 관련 빈이 등록되지 않는다. */
    private boolean enabled;

    /**
     * 조회 라우팅 전략.
     * "auto" — enabled 여부에 따라 자동 선택 (기본값).
     * "mart-only" — Mongo Mart 어댑터만 사용.
     * "oltp-only" — OLTP 어댑터만 사용.
     */
    private String routing = "auto";

    private Rebuild rebuild = new Rebuild();
    private Scheduler scheduler = new Scheduler();
    private LineAccel lineAccel = new LineAccel();
    private Bootstrap bootstrap = new Bootstrap();
    private Mongo mongo = new Mongo();
    private CountIndex countIndex = new CountIndex();

    /** Mart 재빌드 관련 크기 설정. */
    @Getter
    @Setter
    public static class Rebuild {
        /** OLTP에서 한 번에 읽어올 행 수. */
        private int batchSize = 2000;
        /** MongoDB 드라이버 fetchSize 힌트. */
        private int fetchSize = 2000;
        /**
         * full rebuild 병렬 워커 수.
         * freight_header_id 레인지를 n분할해 동시 처리한다.
         * 환경변수 PMS_MART_PARALLELISM으로 오버라이드 가능.
         */
        private int parallelism = 4;
    }

    /** Mart 증분 동기화 스케줄러 설정. */
    @Getter
    @Setter
    public static class Scheduler {
        /** 스케줄러 활성 여부. PmsMartConfig 등록 조건과 독립적으로 제어 가능. */
        private boolean enabled;
        /** Spring cron 표현식. */
        private String cron;
        /** 실행 시작 전 임의 지연 최대값(ms). 여러 인스턴스 동시 기동 분산 목적. */
        private long jitterMaxMs = 15000;
        /** 워터마크 중첩 구간(초). 증분 sync 누락 방지를 위해 겹쳐서 조회. */
        private int watermarkOverlapSeconds = 10;
    }

    /** line-grain(실적일자/서류일자 + 차원) 가속 설정. OFF(기본)면 sidecar 미적재·미조회 = Mart 동작 오늘과 동일. */
    @Getter
    @Setter
    public static class LineAccel {
        /** line-grain sidecar 적재/인덱스/조회 활성. 기본 false. */
        private boolean enabled;

        /**
         * 적응형 page 경로 임계값.
         * count가 이 값을 초과하면(밀집) blId DESC + $elemMatch 조기종료 find 경로를 선택한다.
         * count가 이 값 이하이면(희소) 기존 sidecar pageBlKeys 경로를 유지한다.
         * 환경변수 PMS_MART_LINE_EARLY_TERM_THRESHOLD로 오버라이드 가능.
         */
        private long earlyTermThreshold = 20000;

        /**
         * $sample 근사 추정에 사용할 샘플 크기.
         * 컬렉션의 5% 미만이면 MongoDB는 random-cursor 방식을 사용하므로 sub-second.
         * 환경변수 PMS_MART_APPROX_SAMPLE_SIZE로 오버라이드 가능.
         */
        private long approxSampleSize = 2000;

        /**
         * 조회 결과(카운트·키셋 경계) 캐시 TTL(초).
         * 동일 필터 조합 반복 조회 시 count 재계산·skip 스캔을 생략한다.
         * 환경변수 PMS_MART_CACHE_TTL_SECONDS로 오버라이드 가능.
         */
        private long cacheTtlSeconds = 60;

        /**
         * 캐시 항목 최대 개수(사용자 × 필터 조합 수).
         * 초과 시 가장 오래된 항목부터 정리한다.
         */
        private int cacheMaxSize = 500;

        /**
         * exact count aggregation에 적용하는 maxTime(ms).
         * 신규 조회 killOp이 race로 늦을 경우 최후 백스톱 역할을 한다.
         * 환경변수 PMS_MART_EXACT_COUNT_TIMEOUT_MS로 오버라이드 가능.
         */
        private long exactCountTimeoutMs = 30000;

        /**
         * 깊은 페이지 점프 판정 offset 임계값.
         * 경계 캐시 miss + pageable.getOffset() 이 이 값을 초과하면
         * skip 대신 사이드카 경로로 우회한다.
         * 0페이지·순차(경계 hit)는 이 값과 무관하게 종래 경로를 유지한다.
         * 환경변수 PMS_MART_DEEP_JUMP_OFFSET_THRESHOLD로 오버라이드 가능.
         */
        private long deepJumpOffsetThreshold = 3000;
    }

    /** 기동 시 Mart 자동 빌드 설정. */
    @Getter
    @Setter
    public static class Bootstrap {
        /**
         * true(기본)이면 기동 시 Mart가 비어 있을 때 백그라운드 full rebuild를 자동 실행한다.
         * false이면 Mart가 비어 있어도 rebuild를 실행하지 않고 OLTP 폴백 상태를 유지한다.
         * 환경변수 PMS_MART_BOOTSTRAP_AUTO_REBUILD로 오버라이드 가능.
         */
        private boolean autoRebuild = true;
    }

    /** Mongo 클라이언트 타임아웃(회로차단기가 빠르게 열리도록 기본 30s 대비 하향). */
    @Getter
    @Setter
    public static class Mongo {
        /**
         * 서버 선택 타임아웃(ms). 기본 드라이버 30s라 하향하지 않으면 Mongo 다운 시
         * 차단기가 열리기 전 첫 요청들이 30s씩 매달린다. mongo:7 standalone이라 election window 없음.
         * 환경변수 PMS_MART_MONGO_SERVER_SELECTION_TIMEOUT_MS로 오버라이드.
         */
        private long serverSelectionTimeoutMs = 3000;
        /**
         * 소켓 connect 타임아웃(ms). socket read 타임아웃은 건드리지 않는다
         * (긴 exact-count 집계·full rebuild가 죽으면 안 됨).
         * 환경변수 PMS_MART_MONGO_CONNECT_TIMEOUT_MS로 오버라이드.
         */
        private int connectTimeoutMs = 2000;
    }

    /** routing이 "mart-only"인지 확인. */
    public boolean isMartOnly() {
        return "mart-only".equalsIgnoreCase(routing);
    }

    /** Redis 역색인 기반 정확 distinct-count 가속 설정. */
    @Getter
    @Setter
    public static class CountIndex {
        /**
         * Redis 역색인 활성 여부. false(기본)이면 Redis 관련 빈이 등록되지 않는다.
         * 환경변수 PMS_MART_COUNT_INDEX_ENABLED로 오버라이드 가능.
         */
        private boolean enabled = false;

        /** Redis 키 공통 prefix. 기본 "pms:ix". */
        private String keyPrefix = "pms:ix";

        /**
         * Redis 역색인 허용 stale 구간(초).
         * meta.syncAt 이 Mart lastSyncAt 보다 이 값 초과 지연이면 not-ready → Mongo 폴백.
         */
        private long staleToleranceSeconds = 120;

        /**
         * exact count 연산 대상 최대 ordinal 수.
         * MGET 키 수가 이 값을 초과하는 날짜 범위 쿼리는 Mongo 폴백(JVM 비용 상한).
         */
        private long maxDistinctScan = 2000000;

        /**
         * perfdt 일버킷 계산을 허용하는 최대 일수.
         * from~to 일수가 이 값을 초과하는 경우 freight 경로 null 반환(Mongo 폴백).
         * 환경변수 PMS_MART_COUNT_INDEX_MAX_DAY_BUCKETS로 오버라이드 가능.
         */
        private long maxDayBuckets = 1500;
    }
}
