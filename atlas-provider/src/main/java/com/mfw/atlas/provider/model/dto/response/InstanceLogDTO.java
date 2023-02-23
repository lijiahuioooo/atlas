package com.mfw.atlas.provider.model.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 实例kbs信息推送记录表
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Data
public class InstanceLogDTO implements Serializable {

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
     * 实例状态
     */
    private Integer state;

    /**
     * 健康监测状态
     */
    @TableField("healthState")
    private Integer healthState;

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
