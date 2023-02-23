package com.mfw.atlas.provider.constant;

/**
 * @author KL
 * @Time 2020/10/29 11:00 上午
 */
public enum InstanceChangeEnum {
    /**
        推送到SDK和网关类型
     */
    SDK_GATEWAY_SYN(0,1,"sdk gateway syn"),
    /**
     * 只推送到网关类型
     */
    GATEWAY_SYN(1,4, "gateway syn"),
    GATEWAY_SYN_ALL(1,5, "gateway syn all"),

    /**
     * 只推送到SDK
     */
    SDK_SYN(2, 1, "sdk sync");

    private int type;
    private Integer code;
    private String desc;

    InstanceChangeEnum(int type,Integer code, String desc) {
        this.type=type;
        this.code = code;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static InstanceChangeEnum getEnumByCode(Integer code) {
        //循环处理
        InstanceChangeEnum[] values = InstanceChangeEnum.values();
        for (InstanceChangeEnum enumObject : values) {
            if (enumObject.getCode().equals(code)) {
                return enumObject;
            }
        }
        return null;
    }

}


