package com.mfw.atlas.provider.constant;

public enum ProtocolEnum {

    HTTP(1, "http","spring cloud"),
    DUBBO(2, "dubbo","dubbo"),
    GRPC(3, "grpc","grpc"),
    UDP(4, "udp","udp");

    private Integer type;
    private String protocol;
    private String name;

    ProtocolEnum(Integer type, String protocol,String name) {
        this.type = type;
        this.protocol = protocol;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getName() {
        return name;
    }

    public static ProtocolEnum getEnumByType(int type) {
        //循环处理
        ProtocolEnum[] values = ProtocolEnum.values();
        for (ProtocolEnum enumObject : values) {
            if (enumObject.getType().equals(type)) {
                return enumObject;
            }
        }
        return null;
    }


}
