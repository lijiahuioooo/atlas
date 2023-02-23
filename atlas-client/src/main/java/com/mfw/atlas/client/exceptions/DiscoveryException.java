package com.mfw.atlas.client.exceptions;

import com.mfw.atlas.client.constants.DiscoveryExceptionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class DiscoveryException extends RuntimeException {
    private Integer code;
    private String message;

    public DiscoveryException(DiscoveryExceptionEnum exceptionEnum) {
        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
    }

    public DiscoveryException(DiscoveryExceptionEnum exceptionEnum, String message) {
        this.code = exceptionEnum.getCode();
        this.message = message;
    }

    public DiscoveryException(String message, Integer code) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
