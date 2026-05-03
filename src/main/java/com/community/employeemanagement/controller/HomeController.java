package com.community.employeemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Root URL redirect so https://your-app.onrender.com/ opens the EMS login UI.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login.html";
    }
}
