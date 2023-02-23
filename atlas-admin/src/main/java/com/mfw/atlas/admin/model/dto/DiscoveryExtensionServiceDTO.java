package com.mfw.atlas.admin.model.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 发现中心扩展服务
 *
 * @author ray
 * @since 2020-12-22
 */
@Data
public class DiscoveryExtensionServiceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 应用code
     */
    private String appcode;

    /**
     * 环境类型:dev、beta、product、online
     */
    private String envType;

    /**
     * 扩展信息
     */
    private String metadata;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 修改时间
     */
    private Date mtime;


}
