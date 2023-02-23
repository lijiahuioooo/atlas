package com.mfw.atlas.client.constants;

public enum DiscoveryExceptionEnum {
    // 系统通用
    GET_SERVICE_INSTANCE_FAILED(10000, "获取服务实例失效"),
    GET_SERVICE_INSTANCE_NULL(10001, "获取服务实例返回值为NULL"),
    GET_SERVICE_INSTANCE_ERROR(10002, "获取服务实例返回错误"),
    ;

    private Integer code;
    private String message;

    DiscoveryExceptionEnum(Integer code, String message ) {
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
