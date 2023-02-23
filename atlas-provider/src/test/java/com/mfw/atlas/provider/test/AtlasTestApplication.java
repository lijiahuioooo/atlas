package com.mfw.atlas.provider.test;


import com.mfw.atlas.provider.AtlasApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AtlasApplication.class)
@WebAppConfiguration
public class AtlasTestApplication {

}
