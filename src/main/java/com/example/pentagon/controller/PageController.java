package com.example.pentagon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/booking")
    public String booking() { return "booking"; }

    @GetMapping("/news")
    public String news() { return "news"; }

//    @GetMapping("/community")
//    public String community() { return "community"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/service")
    public String service() { return "service"; }

    @GetMapping("/subscription")
    public String subscription() { return "subscription"; }

    @GetMapping("/subscription/checkout")
    public String subscriptionCheckout() {
        return "subscription_checkout";
    }

    @GetMapping("/info")
    public String info() { return "info"; }

    @GetMapping("/price")
    public String price() { return "price"; }
}
