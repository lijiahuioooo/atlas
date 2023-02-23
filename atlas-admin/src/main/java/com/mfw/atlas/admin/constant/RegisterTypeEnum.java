package com.mfw.atlas.admin.constant;

import java.util.Arrays;

/**
 * @author liuqi
 */

public enum RegisterTypeEnum {

    /**
     * spring cloud service
     */
    ZOOKEEPER(1, "zookeeper"),
    /**
     * dubbo service
     */
    NACOS(2, "nacos"),
    /**
     * mfwdiscovery service
     */
    MFW(3, "mfwdiscovery"),
    ;

    private int code;
    private String name;

    RegisterTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RegisterTypeEnum getByCode(Integer code) {
        return Arrays.stream(values()).filter(v -> v.getCode() == code).findAny().orElse(null);
    }

    public static RegisterTypeEnum getByName(String name) {
        return Arrays.stream(values()).filter(v -> v.getName().equals(name)).findAny().orElse(null);
    }
}
