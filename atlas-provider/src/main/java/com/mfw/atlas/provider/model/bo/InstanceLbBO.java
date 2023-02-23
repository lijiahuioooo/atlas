package com.mfw.atlas.provider.model.bo;

import com.mfw.atlas.provider.model.po.InstancePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstanceLbBO implements java.io.Serializable{

    private String instanceId;

    private InstancePO instancePO;

    private Integer weight;
}
