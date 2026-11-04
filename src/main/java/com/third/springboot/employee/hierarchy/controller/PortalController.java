package com.third.springboot.employee.hierarchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {

    @GetMapping({"/", "/portal"})
    public String showPortal(Model model) {
        model.addAttribute("title", "Hierarchical Organization Management Portal");
        return "portal";
    }
}
