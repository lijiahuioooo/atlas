package com.mfw.atlas.client.constants;

import java.util.Arrays;

/**
 * @author liuqi
 */

public enum ClientTypeEnum {
    /**
     * java客户端
     */
    JAVA(1),
    /**
     * php客户端
     */
    PHP(2),

    GO(3);
    private int code;

    ClientTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ClientTypeEnum getByCode(Integer code) {
        return Arrays.stream(values()).filter(v -> v.getCode() == code).findAny().orElse(null);
    }
}
