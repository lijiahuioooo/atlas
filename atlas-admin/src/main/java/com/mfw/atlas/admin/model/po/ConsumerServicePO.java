package com.mfw.atlas.admin.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 消费方信息表
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("t_consumer_service")
public class ConsumerServicePO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务分组
     */
    private String serviceGroup;

    /**
     * spring cloud /dubbo
     */
    private Integer serviceType;

    /**
     * 注册中心类型，zk/acos
     */
    private Integer registerType;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 扩展数据
     */
    private String metadata;

    /**
     * 服务版本号
     */
    private String serviceVersion;

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
