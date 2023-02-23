package com.mfw.atlas.admin.constant;

public enum InstanceStatusEnum {
    /**
     * k8s 实例状态
     */
    UNKNOWN(0, "unknown"),
    ENABLE(1, "enable"),
    DISABLE(2, "disable"),
    CHANGE(3, "change");

    private Integer code;

    private String desc;

    InstanceStatusEnum(Integer code, String desc) {
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


    public static InstanceStatusEnum getEnumByCode(Integer code) {
        //循环处理
        InstanceStatusEnum[] values = InstanceStatusEnum.values();
        for (InstanceStatusEnum enumObject : values) {
            if (enumObject.getCode().equals(code)) {
                return enumObject;
            }
        }
        return null;
    }

}
