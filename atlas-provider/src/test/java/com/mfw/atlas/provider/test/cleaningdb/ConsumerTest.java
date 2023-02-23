package com.mfw.atlas.provider.test.cleaningdb;

import com.mfw.atlas.provider.AtlasApplication;
import com.mfw.atlas.provider.manager.ConsumerServiceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AtlasApplication.class)
@WebAppConfiguration
public class ConsumerTest {

    static com.netflix.curator.framework.CuratorFramework client = null;

    @Autowired
    private ConsumerServiceManager consumerServiceManager;


    @Test
    public void removeProviderInstances(){
        consumerServiceManager.removeOfflineConsumerInstances(500,getPastDate(0));
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

    public static void main(String[] args)  {

    }
}
