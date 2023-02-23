package com.mfw.atlas.admin.model.po;

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
 * 实例端口表
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
@TableName("t_instance_port")
public class InstancePortPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 端口名
     */
    private String name;

    /**
     * port
     */
    private String port;

    /**
     * 协议
     */
    private String protocol;

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
