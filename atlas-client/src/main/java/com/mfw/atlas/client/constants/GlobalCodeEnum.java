package com.mfw.atlas.client.constants;

/**
 * @author jiangqiao
 */
public enum GlobalCodeEnum {

    /**
     * 全局返回码定义 - 0开头
     */
    GL_SUCC_0000(0, "成功"),
    GL_FAIL_9995(995, "参数异常"),
    GL_FAIL_9998(998, "参数错误"),
    GL_FAIL_9999(999, "系统异常"),
    GL_REGISTER_FAIL_9001(9001, "上报注册元数据系异常"),
    GL_SUBSCRIBE_FAIL_9002(9002, "上报订阅元数据系异常"),
    GL_EMPTY_SERVICE_FAIL_9003(9003, "提供方没有可用实例"),
    GL_REGISTERTYPE_FAIL_9004(9004, "原注册类型错误"),
    GL_NOTSUPPORT_REGISTERTYPE_FAIL_9005(9005, "暂不支持springcloud服务，注册中心为zk的数据拉取。"),
    GL_ORIGINAL_ADDRESS_FAIL_9006(9006, "请检查SDK中mfw.original.address配置！"),
    GL_CONNECTION_REGISTER_FAIL_9007(9007, "连接注册中心失败"),
    GL_CREATE_NACOS_CLIENT_FAIL_9008(9008, "创建nacosClient错误"),
    GL_NACOS_API_FAIL_9009(9009, "获取nacos数据的API无法请求"),
    GL_ZOOKEEPER_CLIENT_FAIL_9010(9010, "zkClient获取注册数据失败"),
    GL_INSERT_FAIL_9011(9011, "添加失败，请稍后再试！"),
    ;
    /**
     * 编码
     */
    private Integer code;

    /**
     * 描述
     */
    private String desc;


    GlobalCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举类型
     *
     * @param code 编码
     * @return
     */
    public static GlobalCodeEnum getByCode(String code) {
        //判空
        if (code == null) {
            return null;
        }
        //循环处理
        GlobalCodeEnum[] values = GlobalCodeEnum.values();
        for (GlobalCodeEnum value : values) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
