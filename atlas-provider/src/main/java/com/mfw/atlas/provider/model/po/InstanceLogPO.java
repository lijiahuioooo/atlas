package com.mfw.atlas.provider.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 实例kbs信息推送记录表
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
@TableName("t_instance_log")
public class InstanceLogPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 日志类型，新增、修改、删除
     */
    private Integer logType;

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
     * 实例信息
     */
    private String instanceInfo;
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
    private LocalDateTime ctime;

    /**
     * 修改时间
     */
    private LocalDateTime mtime;


}
