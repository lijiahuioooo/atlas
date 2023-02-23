package com.mfw.atlas.provider.model.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 实例端口表
 * </p>
 *
 * @author jobob
 * @since 2020-10-21
 */
@Data
public class InstancePortDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 实例id
     */
    private String instanceId;

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
