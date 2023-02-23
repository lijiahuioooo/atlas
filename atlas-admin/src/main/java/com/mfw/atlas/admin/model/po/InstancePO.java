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
 * 实例信息表
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
@TableName("t_instance")
public class InstancePO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 服务编码
     */
    private String appCode;

    /**
     * 服务编码
     */
    private String appName;

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
     * 实例所属集群
     */
    private String cluster;

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
     * cpu个数
     */
    private Integer cpu;

    /**
     * 内存大小
     */
    private Integer memory;

    /**
     * 磁盘大小
     */
    private Integer disk;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 镜像
     */
    private String image;

    /**
     * 扩展信息
     */
    private String label;

    /**
     * hostname
     */
    private String hostname;

    /**
     * 机房
     */
    private String idc;

    /**
     * 是否处于上线的状态
     */
    private Integer enabled;

    /**
     * 实例状态，用于服务发现
     */
    private Integer status;

    /**
     * 实例状态
     */
    private String state;

    /**
     * 健康监测状态
     */
    private String healthState;

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
