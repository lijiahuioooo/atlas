package com.mfw.atlas.provider.model.dto.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class QueryInstanceRequestDTO implements Serializable {
    @NotBlank(message = "缺少appcode")
    private String appcode;

    @NotBlank(message = "envType不能为空")
    private String envType;

    private String envGroup;

    private Integer status;

    private String version;

    private String cluster;

    private String idc;
}
