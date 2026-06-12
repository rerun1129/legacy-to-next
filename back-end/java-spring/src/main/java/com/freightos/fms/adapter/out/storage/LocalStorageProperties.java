package com.freightos.fms.adapter.out.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@ConfigurationProperties("fms.storage.local")
public class LocalStorageProperties {

    /** 첨부파일 저장 루트 디렉토리. */
    private String basePath = "./storage/attachments";

    public Path resolvedBasePath() {
        return Paths.get(basePath).toAbsolutePath().normalize();
    }
}
