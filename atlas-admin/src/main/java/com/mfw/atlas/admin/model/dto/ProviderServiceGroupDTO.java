package com.mfw.atlas.admin.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderServiceGroupDTO implements Serializable {

    private String serviceName;
    private String appCode;
    private int instanceSize;


    public ProviderServiceGroupDTO(String key, String code, int size) {
        serviceName = key;
        appCode = code;
        instanceSize = size;
    }
}
