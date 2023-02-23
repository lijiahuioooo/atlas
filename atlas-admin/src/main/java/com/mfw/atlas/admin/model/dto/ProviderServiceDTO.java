package com.mfw.atlas.admin.model.dto;

import com.mfw.atlas.admin.constant.RegisterTypeEnum;
import com.mfw.atlas.admin.constant.ServiceTypeEnum;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author huangrui
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ProviderServiceDTO {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * ip
     */
    private String ip;

    /**
     * port
     */
    private String port;

    /**
     * 环境类型:dev、beta、product、online
     */
    private String envType;

    /**
     * 环境唯一码，一般为：env_type + env_group
     */
    private String envCode;

    /**
     * 环境分组
     */
    private String envGroup;

    /**
     * 实例部署的版本号
     */
    private String version;

    /**
     * 实例变化的版本号,是用来保证数据不被回滚
     */
    private Long reversion;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务分组
     */
    private String serviceGroup;

    /**
     * 服务版本号
     */
    private String serviceVersion;

    /**
     * spring cloud /dubbo
     */
    private ServiceTypeEnum serviceType;

    /**
     * 注册中心类型，zk
     acos
     */
    private RegisterTypeEnum registerType;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 扩展数据
     */
    private String metadata;

    /**
     * 实例状态
     */
    private Integer state;

    /**
     * 服务key
     */
    private String serviceKey;

    /**
     * 是否删除 0:未删除 1:删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 修改时间
     */
    private Date mtime;
}
