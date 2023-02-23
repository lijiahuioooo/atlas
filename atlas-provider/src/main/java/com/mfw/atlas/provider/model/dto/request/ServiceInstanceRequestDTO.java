package com.mfw.atlas.provider.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mfw.atlas.provider.util.GsonUtils;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

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

    private Map<String,Object> getExtendedParamsMap() {
        if (StringUtils.isBlank(extendedMFWParams)) {
            return null;
        }
        if (extendedParamsMap == null) {
            synchronized (this) {
                if (extendedParamsMap == null) {
                    extendedParamsMap =  GsonUtils.fromJson(UriUtils.decode(extendedMFWParams, Charset.defaultCharset()),Map.class);
                }
            }
        }
        return extendedParamsMap;
    }



}
