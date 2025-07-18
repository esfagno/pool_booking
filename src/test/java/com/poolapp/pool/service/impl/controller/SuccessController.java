package com.poolapp.pool.service.impl.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuccessController {

    //done it just for checking front
    @GetMapping("/success")
    public String successPage() {
        return "success";
    }
}

