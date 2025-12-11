package com.kapamejlbka.objectmanager.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    @GetMapping("/login")
    @ResponseBody
    public String loginPageDebug(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout) {

        return "LOGIN CONTROLLER OK, error=" + (error != null) + ", logout=" + (logout != null);
    }
}
