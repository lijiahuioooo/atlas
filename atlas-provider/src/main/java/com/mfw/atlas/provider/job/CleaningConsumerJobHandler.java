package com.mfw.atlas.provider.job;

import com.mfw.atlas.provider.manager.ConsumerServiceManager;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@JobHandler(value = "cleaningConsumerJobHandler")
@Component
@Slf4j
public class CleaningConsumerJobHandler extends IJobHandler {

    @Autowired
    private ConsumerServiceManager consumerServiceManager;

    @Override
    public ReturnT<String> execute(String param) throws Exception {

        try{
            int limit = 500;
            String pastDate = getPastDate(0);

            consumerServiceManager.removeOfflineConsumerInstances(limit,pastDate);

        }catch (Exception e){
            XxlJobLogger.log("CleaningConsumerJobHandler执行异常："+e.getMessage());
        }


        return SUCCESS;
    }

    /**
     * 获取过去第几天的日期
     * @param past
     * @returns
     */
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        return result;
    }


}
