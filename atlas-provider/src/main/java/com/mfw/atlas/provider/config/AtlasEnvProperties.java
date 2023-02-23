package com.mfw.atlas.provider.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author KL
 * @Time 2020/10/22 3:52 下午
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mfw.env", ignoreUnknownFields = true)
public class AtlasEnvProperties {

    private int datagramSocketPort = 13505;
    /**
     * 不配置则不过滤
     */
	private List<String> instanceEnvMap;
}
