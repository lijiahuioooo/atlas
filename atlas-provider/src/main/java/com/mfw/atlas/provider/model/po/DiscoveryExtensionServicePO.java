package com.mfw.atlas.provider.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发现中心扩展服务
 *
 * @author ray
 * @since 2020-12-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("t_discovery_extension_service")
public class DiscoveryExtensionServicePO implements Serializable {

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
     * 版本信息
     */
    private String versions;

    /**
     * 创建时间
     */
    private Date ctime;

    /**
     * 修改时间
     */
    private Date mtime;


}
