package com.freightos.fms.adapter.out.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("fms.storage.cleanup")
public class AttachmentCleanupProperties {

    private boolean enabled = true;
    private String cron = "0 0 3 * * *";

    /** 고아 파일로 판정하기 전 유예 시간(시간 단위). */
    private long graceHours = 24;
}
