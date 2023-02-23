package com.mfw.atlas.admin.model.bo;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
@Builder
public class InstanceLogInfoBO {

    /**
     * 实例id
     */
    private String instanceId;
    /**
     * 服务编码
     */
    private String appCode;

    /**
     * ip
     */
    private String ip;

    /**
     * 环境类型:dev、beta、product、online
     */
    private String envType;

    /**
     * 环境分组
     */
    private String envGroup;

    /**
     * 环境唯一码，一般为：env_type + env_group
     */
    private String envCode;

    /**
     * 实例部署的版本号
     */
    private String version;

    /**
     * 实例变化的版本号,是用来保证数据不被回滚
     */
    private Long reversion;

    /**
     * 实例提供方,k8s,ecs
     */
    private String provider;

    /**
     * 扩展信息
     */
    private String labels;

    /**
     * hostname
     */
    private String hostname;

    /**
     * 机房
     */
    private String idc;

    private List<InstanceLogPortBO> portBOS;

    @Data
    @Builder
    public static class InstanceLogPortBO{
        /**
         * port
         */
        private String port;

        /**
         * 协议
         */
        private String protocol;
    }


}
