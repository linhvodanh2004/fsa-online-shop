package fsa.project.online_shop.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VnPayController {
    @GetMapping("/payment")
    public String getPayment() {
        return "";
    }
}
