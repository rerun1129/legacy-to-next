package com.freightos.admin.common.response;

public record SaveChangesResult(int createdCount, int updatedCount, int deletedCount) {}
