package com.freightos.common.exception;

import com.freightos.fms.common.response.MessageCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends FmsException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
              "%s not found: %s".formatted(resourceName, id));
    }

    public ResourceNotFoundException(MessageCode messageCode) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", messageCode.message());
    }
}
