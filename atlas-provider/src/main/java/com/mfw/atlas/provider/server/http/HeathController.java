package com.mfw.atlas.provider.server.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore()
@RestController
public class HeathController {

    @RequestMapping("/")
    public String heath() {
        return "SUCCESS";
    }
}
