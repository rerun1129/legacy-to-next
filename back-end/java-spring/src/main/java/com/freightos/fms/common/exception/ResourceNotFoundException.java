package com.freightos.fms.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends FmsException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
              "%s not found: %s".formatted(resourceName, id));
    }
}
