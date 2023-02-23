package com.mfw.atlas.provider.model.bo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AlarmBO {

    /**
     * 报警人信息
     */
    private List<String> userInfos;
    /**
     * 报警内容
     */
    private String content;
}
