package com.mfw.atlas.client.constants;

public enum EnvExceptionEnum {
    // 系统通用
    ENV_INCOMPLETE(10000, "环境信息不完整"),
    ENV_INVALID_APPNAME(10001, "application name和环境变量APPNAME不一致"),
    ;

    private Integer code;
    private String message;

    EnvExceptionEnum(Integer code, String message ) {
        this.code = code;
        this.message = message;
    }

    public final Integer getCode() {
        return this.code;
    }

    public final String getMessage() {
        return this.message;
    }

}
