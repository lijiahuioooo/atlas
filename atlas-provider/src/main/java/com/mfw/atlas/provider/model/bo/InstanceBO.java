package com.mfw.atlas.provider.model.bo;

import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.model.po.InstancePortPO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IP 与端口信息汇总
 *
 * @author KL
 * @Time 2020/11/2 4:20 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstanceBO implements  java.io.Serializable{
    /**
     * 实例id
     */
    private String instanceId;

    private InstancePO instancePO;

    private List<InstancePortPO> instancePortPOS;

}
