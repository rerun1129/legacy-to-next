package com.freightos.admin.application.user.projection;

public enum UserScope {
    ALL,        // deletedAt IS NULL 모든 사용자 (활성 + 비활성)
    ACTIVE,     // deletedAt IS NULL AND active=true
    INACTIVE,   // deletedAt IS NULL AND active=false
    DELETED     // deletedAt IS NOT NULL
}
