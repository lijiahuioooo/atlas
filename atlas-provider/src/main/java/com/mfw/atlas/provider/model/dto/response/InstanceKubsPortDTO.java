package com.mfw.atlas.provider.model.dto.response;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关端口proto关系表
 *
 * @author KL
 * @Time 2020/11/2 4:20 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceKubsPortDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 端口名
     */
    private String name;

    /**
     * port
     */
    private Integer port;

    /**
     * 协议
     */
    private String protocol;

}
