package com.mfw.atlas.provider.model.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * 网关实例信息表
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Data
public class GatewayInstanceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例id
     */
    @NotEmpty( message = "param instanceId can not be null")
    private String instanceId;

    /**
     * ip
     */
    @NotEmpty( message = "param ip can not be null")
    private String ip;

    /**
     * port
     */
    @NotEmpty( message = "param port can not be null")
    private String port;

    /**
     * 环境类型:dev、beta、product、online
     */
    @NotEmpty( message = "param envType can not be null")
    private String envType;

    /**
     * 是否启用此实例
     */
    @NotNull( message = "param enabled can not be null")
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
