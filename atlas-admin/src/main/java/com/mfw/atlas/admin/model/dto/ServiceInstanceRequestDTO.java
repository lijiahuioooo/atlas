package com.mfw.atlas.admin.model.dto;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author fenghua
 */
@Data
public class ServiceInstanceRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String instanceId;

    @Min(value = 1,message = "serviceType无效,不能小于1")
    @Max(value = 2,message = "serviceType无效,不能大于2")
    private int serviceType;

    @NotBlank(message = "serviceName无效")
    private String serviceName;

    private String serviceGroup;

    private String serviceVersion;

    @NotBlank(message = "clientVersion无效")
    private String clientVersion;

    @NotBlank(message = "envType无效")
    private String envType;

    private String envGroup;

    @NotEmpty(message = "extendedMFWParams不能为空")
    private String extendedMFWParams;

    @JsonIgnore
    private transient Map<String,Object> extendedParamsMap;

    public Object getExtendedMFWParams(String key) {
        if (StringUtils.isBlank(extendedMFWParams)) {
            return null;
        }
        return getExtendedParamsMap().get(key);
    }




}
