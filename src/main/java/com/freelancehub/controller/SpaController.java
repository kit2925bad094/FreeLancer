package com.freelancehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    /**
     * Forwards direct browser URLs (without hash) to index.html
     * so that client-side SPA routing can handle them without 404s.
     */
    @GetMapping(value = {
        "/projects",
        "/freelancers",
        "/dashboard",
        "/applications",
        "/profile"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
