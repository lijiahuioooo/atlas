package com.mfw.atlas.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeathController {

    @RequestMapping("/")
    public String heath() {
        return "SUCCESS111";
    }
}
