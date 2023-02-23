package com.mfw.atlas.provider.model.dto.request;

import java.io.Serializable;
import lombok.Data;

@Data
public class GetAllInstancesRequestDTO implements Serializable {
    private int status;

    private String provider;
}
