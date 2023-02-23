package com.mfw.atlas.client.exceptions;

import com.mfw.atlas.client.constants.EnvExceptionEnum;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class EnvException extends RuntimeException {
    private Integer code;
    private String message;

    public EnvException(EnvExceptionEnum exceptionEnum) {
        this.code = exceptionEnum.getCode();
        this.message = exceptionEnum.getMessage();
    }

    public EnvException(EnvExceptionEnum exceptionEnum, String message) {
        this.code = exceptionEnum.getCode();
        this.message = message;
    }

    public EnvException(String message, Integer code) {
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
