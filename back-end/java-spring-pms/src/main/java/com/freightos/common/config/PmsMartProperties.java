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

    /** routing이 "mart-only"인지 확인. */
    public boolean isMartOnly() {
        return "mart-only".equalsIgnoreCase(routing);
    }
}
