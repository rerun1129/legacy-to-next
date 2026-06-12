package com.freightos.fms.adapter.out.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 스토리지 설정.
 *
 * ⚠️ key-prefix는 최초 설정 후 불변.
 * 변경 시 기존 객체가 list()에서 빠져 unmanaged 잔존 상태가 되므로
 * prefix 변경이 필요하면 기존 객체 전체 마이그레이션 후 적용한다.
 */
@Getter
@Setter
@ConfigurationProperties("fms.storage.s3")
public class S3StorageProperties {

    private String bucket = "";
    private String region = "ap-northeast-2";
    private String keyPrefix = "";
    private String endpoint = "";

    /**
     * S3 object key prefix를 정규화한다.
     * 빈 값이면 그대로 반환하고, 값이 있으면 끝에 '/'를 보장한다.
     */
    public String normalizedKeyPrefix() {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            return "";
        }
        return keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/";
    }
}
