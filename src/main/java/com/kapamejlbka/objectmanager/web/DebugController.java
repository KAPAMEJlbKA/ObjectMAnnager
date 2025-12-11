package com.kapamejlbka.objectmanager.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DebugController {

    @GetMapping("/ping-open")
    @ResponseBody
    public String pingOpen() {
        return "OK: no auth";
    }

    @GetMapping("/ping-secured")
    @ResponseBody
    public String pingSecured() {
        return "OK: secured";
    }
}
