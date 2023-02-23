package com.mfw.atlas.admin.constant;

import java.util.Arrays;

/**
 * @author liuqi
 */

public enum ServiceTypeEnum {

    /**
     * spring cloud service
     */
    SPRING_CLOUD(1),
    /**
     * dubbo service
     */
    DUBBO(2),
    ;

    private int code;

    ServiceTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ServiceTypeEnum getByCode(Integer code) {
        return Arrays.stream(values()).filter(v -> v.getCode() == code).findAny().orElse(null);
    }
}
