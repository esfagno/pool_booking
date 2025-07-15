package com.poolapp.pool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegistrationController {

    //done it just for checking front
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }
}

