package com.mfw.atlas.provider;


import com.mfw.middleware.MonitorEnableAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ServletComponentScan
@EnableWebMvc
@MonitorEnableAutoConfiguration
@MapperScan("com.mfw.atlas.provider.dao")
@EnableTransactionManagement
@EnableSwagger2
public class AtlasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasApplication.class, args);
    }

}
