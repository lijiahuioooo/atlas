package com.mfw.atlas.provider.constant;

/**
 * @author KL
 * @Time 2020/10/29 4:19 下午
 */
public enum InstanceEnableEnum {
    ENABLE(1, "enable"),
    DISABLE(0, "disable");

    private Integer code;

    private String desc;

    InstanceEnableEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }


    public static InstanceEnableEnum getEnumByCode(Integer code) {
        //循环处理
        InstanceEnableEnum[] values = InstanceEnableEnum.values();
        for (InstanceEnableEnum enumObject : values) {
            if (enumObject.getCode().equals(code)) {
                return enumObject;
            }
        }
        return null;
    }
}
