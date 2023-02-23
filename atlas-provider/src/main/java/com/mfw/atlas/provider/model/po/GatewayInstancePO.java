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
 * 网关实例信息表
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
@TableName("t_gateway_instance")
public class GatewayInstancePO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 实例id
     */
    private String instanceId;

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
     * 是否启用此实例
     */
    private Integer enabled;

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
