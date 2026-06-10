package com.suraj.scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MiscController {

    @GetMapping("/interview")
    public String interview() {
        return "interview";
    }
}
